package org.hango.cloud.configuration.env;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import istio.mcp.nsf.SnapshotOuterClass;
import istio.mcp.v1alpha1.Mcp;
import istio.mcp.v1alpha1.ResourceOuterClass;
import istio.networking.v1alpha3.*;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.configuration.ext.K8sMultiClusterProperties;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.gateway.GatewayIstioModelEngine;
import org.hango.cloud.core.gateway.service.GatewayConfigManager;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.gateway.service.impl.GatewayConfigManagerImpl;
import org.hango.cloud.core.gateway.service.impl.K8sConfigStore;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.mcp.*;
import org.hango.cloud.mcp.aop.ConfigStoreAop;
import org.hango.cloud.mcp.aop.GatewayServiceAop;
import org.hango.cloud.mcp.dao.ResourceDao;
import org.hango.cloud.mcp.dao.StatusDao;
import org.hango.cloud.mcp.dao.impl.ResourceDaoImpl;
import org.hango.cloud.mcp.dao.impl.StatusDaoImpl;
import org.hango.cloud.mcp.ratelimit.RlsClusterClient;
import org.hango.cloud.mcp.snapshot.DBSnapshotBuilder;
import org.hango.cloud.mcp.snapshot.SnapshotBuilder;
import org.hango.cloud.mcp.status.*;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.service.impl.GatewayServiceImpl;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import slime.microservice.plugin.v1alpha1.PluginManagerOuterClass;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty("nonK8sMode")
public class NonK8sConfiguration {
    @Value("${mcpPort:8899}")
    private Integer port;

    @Value("${mcpStatusCheckInterval:1000}")
    private Long mcpStatusCheckInterval;

    @Value("${mcpKeepaliveTime:30000}")
    private Long mcpKeepaliveTime;

    @Value("${mcpKeepaliveTimeout:10000}")
    private Long mcpKeepaliveTimeout;

    @Value("${mcpMaxMessageSize:134217728}")
    private Integer mcpMaxMessageSize;

    @Value("${rlsAddresses:#{null}}")
    private String rlsAddresses;

    @Bean
    public McpOptions options() {
        McpOptions options = new McpOptions();
        options.setStatusCheckIntervalMs(mcpStatusCheckInterval);
        options.setKeepaliveTime(mcpKeepaliveTime);
        options.setKeepaliveTimeout(mcpKeepaliveTimeout);
        options.setMaxMessageSize(mcpMaxMessageSize);

        options.registerSnapshotCollection(McpResourceEnum.VirtualService.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.Gateway.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.DestinationRule.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.EnvoyPlugin.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.PluginManager.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.ServiceEntry.getCollection());

        options.registerDescriptor(SnapshotOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(ResourceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(VirtualServiceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(DestinationRuleOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(PluginManagerOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayPluginOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(ServiceEntryOuterClass.getDescriptor().getMessageTypes());

        if (!StringUtils.isEmpty(rlsAddresses)) {
            String[] addresses = StringUtils.split(rlsAddresses, ",");
            for (String address : addresses) {
                options.registerRls(address);
            }
        }
        return options;
    }

    /**
     * Monitor
     */
    @Bean
    public StatusMonitor monitor(McpOptions options, StatusProductor productor, SnapshotBuilder builder, McpResourceDistributor distributor, TransactionTemplate transactionTemplate, RlsClusterClient rlsClusterClient) {
        StatusMonitor monitor = new StatusMonitorImpl(options.getStatusCheckIntervalMs(), productor);
        monitor.registerHandler(StatusConst.RESOURCES_VERSION, ((event, property) -> {
            Logger logger = LoggerFactory.getLogger(builder.getClass());
            SnapshotOuterClass.Snapshot snapshot = transactionTemplate.execute(new TransactionCallback<SnapshotOuterClass.Snapshot>() {
                @Override
                public SnapshotOuterClass.Snapshot doInTransaction(TransactionStatus transactionStatus) {
                    String thisVersion = property.value;
                    String dbVersion = productor.product().get(StatusConst.RESOURCES_VERSION);
                    if (Objects.equals(thisVersion, dbVersion)) {
                        long start = System.currentTimeMillis();
                        SnapshotOuterClass.Snapshot snapshot = builder.build();
                        logger.info("MCP: SnapshotBuilder: build snapshot for version:[{}], consume:[{}]", snapshot.getVersion(), System.currentTimeMillis() - start + "ms");
                        for (Map.Entry<String, Mcp.Resources> entry : snapshot.getResourcesMap().entrySet()) {
                            logger.info("--MCP: SnapshotBuilder: collection:[{}], count:[{}]", entry.getKey(), entry.getValue().getResourcesList().size());
                        }
                        return snapshot;
                    } else {
                        logger.info("MCP: SnapshotBuilder: Skip building snapshots for outdated resource versions:[{}], current version:[{}]", thisVersion, dbVersion);
                        return null;
                    }
                }
            });
            if (Objects.nonNull(snapshot)) {
                distributor.setSnapshot(snapshot);
            }
        }));
        monitor.registerHandler(StatusConst.RATELIMIT_VERSION, (((event, property) -> {
            rlsClusterClient.sync();
        })));
        // 启动monitor
        monitor.start();
        return monitor;
    }

    /**
     * Grpc Server
     */
    @Bean
    public Server server(McpResourceWatcher watcher, McpOptions options) throws IOException {
        return NettyServerBuilder.forPort(port)
                // 心跳
                .keepAliveTime(options.getKeepaliveTime(), TimeUnit.MILLISECONDS)
                // 心跳超时
                .keepAliveTimeout(options.getKeepaliveTimeout(), TimeUnit.MILLISECONDS)
                // 最大message
                .maxMessageSize(options.getMaxMessageSize())
                .addService(new ResourceSourceImpl(watcher, options))
                .build()
                .start();
    }

    /**
     * Dao
     */
    @Bean
    public ResourceDao resourceDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new ResourceDaoImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public StatusDao statusDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new StatusDaoImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        // 传播级别
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 隔离级别，容忍幻读
        template.setIsolationLevel(TransactionTemplate.ISOLATION_REPEATABLE_READ);
        // 事务超时时间
        template.setTimeout(5);
        return template;
    }

    /**
     * Marshaller
     */
    @Bean
    public McpMarshaller marshaller(McpOptions options) {
        return new McpMarshaller(options);
    }

    /**
     * Distributor
     */
    @Bean
    public McpCache cache() {
        return new McpCache();
    }

    /**
     * SnapshotBuilder
     */
    @Bean
    public SnapshotBuilder snapshotStore(ResourceDao resourceDao, McpMarshaller mcpMarshaller, McpOptions mcpOptions) {
        return new DBSnapshotBuilder(resourceDao, mcpMarshaller, mcpOptions);
    }

    /**
     * Status
     */
    @Bean
    public StatusProductor statusProductor(StatusDao statusDao) {
        return new StatusProductorImpl(statusDao);
    }

    @Bean
    public StatusNotifier statusNotifier(StatusDao statusDao) {
        return new StatusNotifierImpl(statusDao, key -> new Date().toString());
    }

    /**
     * Ratelimit Server Client
     */
    @Bean
    public RlsClusterClient rlsClusterClient(McpOptions mcpOptions, ResourceDao resourceDao, McpMarshaller marshaller) {
        return new RlsClusterClient(mcpOptions, resourceDao, marshaller);
    }

    /**
     * ConfigManager
     */
    @Bean
    public McpConfigStore configStore(ResourceDao resourceDao, GlobalConfig globalConfig) {
        return new McpConfigStore(resourceDao, globalConfig);
    }

    @Bean
    public GatewayConfigManager gatewayConfigManager(GatewayIstioModelEngine modelEngine, McpConfigStore k8sConfigStore, GlobalConfig globalConfig, ApplicationEventPublisher eventPublisher) {
        return new GatewayConfigManagerImpl(modelEngine, k8sConfigStore, globalConfig, eventPublisher);
    }

    /**
     * Service
     */
    @Bean
    public GatewayService gatewayService(ResourceManager resourceManager, GatewayConfigManager configManager, GlobalConfig globalConfig) {
        return new GatewayServiceImpl(resourceManager, configManager, globalConfig);
    }

    /**
     * AOP
     * 1. 为GatewayService开启事务
     * 2. 执行特定方法后更新Status表
     */
    @Bean
    public GatewayServiceAop gatewayServiceAop(TransactionTemplate transactionTemplate, StatusNotifier statusNotifier) {
        return new GatewayServiceAop(transactionTemplate, statusNotifier);
    }

    @Bean
    public ConfigStoreAop configStoreAop(TransactionTemplate transactionTemplate, StatusNotifier statusNotifier) {
        return new ConfigStoreAop(transactionTemplate, statusNotifier);
    }

    /**
     * Mock Bean
     */
    @Bean
    public KubernetesClient kubernetesClient() {
        return Mockito.mock(KubernetesClient.class);
    }

    @Bean
    public K8sConfigStore K8sConfigStore(){
        return Mockito.mock(K8sConfigStore.class);
    }

    @Bean("originalKubernetesClient")
    public io.fabric8.kubernetes.client.KubernetesClient originalKubernetesClient(MultiClusterK8sClient mc) {
        return Mockito.mock(io.fabric8.kubernetes.client.KubernetesClient.class);
    }

    @Bean
    public MultiClusterK8sClient multiClusterK8sClient(K8sMultiClusterProperties properties, EditorContext editorContext) {
        return Mockito.mock(MultiClusterK8sClient.class);
    }
}
