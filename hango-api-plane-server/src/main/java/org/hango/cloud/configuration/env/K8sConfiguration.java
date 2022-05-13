package org.hango.cloud.configuration.env;

import org.hango.cloud.configuration.ext.IstioSupportConfiguration;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.gateway.GatewayIstioModelEngine;
import org.hango.cloud.core.gateway.service.GatewayConfigManager;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.gateway.service.impl.GatewayConfigManagerImpl;
import org.hango.cloud.core.gateway.service.impl.K8sConfigStore;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.service.GatewayService;
import org.hango.cloud.service.impl.GatewayServiceImpl;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/26
 **/
@Configuration
@ConditionalOnMissingBean(NonK8sConfiguration.class)
@ImportAutoConfiguration(IstioSupportConfiguration.class)
public class K8sConfiguration {

    @Bean
    @Primary
    public K8sConfigStore configStore(KubernetesClient client, GlobalConfig globalConfig) {
        return new K8sConfigStore(client, globalConfig);
    }

    @Bean
    public GatewayConfigManager gatewayConfigManager(GatewayIstioModelEngine modelEngine, K8sConfigStore k8sConfigStore, GlobalConfig globalConfig, ApplicationEventPublisher eventPublisher) {
        return new GatewayConfigManagerImpl(modelEngine, k8sConfigStore, globalConfig, eventPublisher);
    }

    @Bean
    public GatewayService gatewayService(ResourceManager resourceManager, GatewayConfigManager configManager, GlobalConfig globalConfig) {
        return new GatewayServiceImpl(resourceManager, configManager, globalConfig);
    }
}
