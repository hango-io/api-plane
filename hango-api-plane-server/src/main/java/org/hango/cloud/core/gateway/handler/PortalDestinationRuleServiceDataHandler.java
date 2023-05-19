package org.hango.cloud.core.gateway.handler;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.Service;
import org.hango.cloud.meta.dto.LocalitySettingDTO;
import org.hango.cloud.meta.dto.PortalServiceConnectionPoolDTO;
import org.hango.cloud.util.CommonUtil;
import org.hango.cloud.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.hango.cloud.core.template.TemplateConst.*;

public class PortalDestinationRuleServiceDataHandler extends ServiceDataHandler {

    private static final Logger logger = LoggerFactory.getLogger(PortalDestinationRuleServiceDataHandler.class);

    private static YAMLMapper yamlMapper;

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, Service service) {
        TemplateParams params = TemplateParams.instance()
                .put(DESTINATION_RULE_NAME, service.getCode() + "-" + service.getGateway())
                .put(DESTINATION_RULE_HOST, service.getBackendService())
                .put(VERSION, service.getVersion())
                .put(NAMESPACE, service.getNamespace())
                .put(API_SERVICE, service.getCode())
                .put(DESTINATION_RULE_CONSECUTIVE_ERRORS, service.getConsecutiveErrors())
                .put(DESTINATION_RULE_BASE_EJECTION_TIME, service.getBaseEjectionTime())
                .put(DESTINATION_RULE_MAX_EJECTION_PERCENT, service.getMaxEjectionPercent())
                .put(DESTINATION_RULE_MIN_HEALTH_PERCENT, service.getMinHealthPercent())
                .put(DESTINATION_RULE_PATH, service.getPath())
                .put(DESTINATION_RULE_TIMEOUT, service.getTimeout())
                .put(DESTINATION_RULE_EXPECTED_STATUSES, service.getExpectedStatuses())
                //当前只支持HTTP健康检查
                .put(DESTINATION_RULE_HEALTHY_CHECKER_TYPE, "http")
                .put(DESTINATION_RULE_HEALTHY_INTERVAL, service.getHealthyInterval())
                .put(DESTINATION_RULE_HEALTHY_THRESHOLD, service.getHealthyThreshold())
                .put(DESTINATION_RULE_UNHEALTHY_INTERVAL, service.getUnhealthyInterval())
                .put(DESTINATION_RULE_UNHEALTHY_THRESHOLD, service.getUnhealthyThreshold())
                .put(DESTINATION_RULE_ALT_STAT_NAME, service.getServiceTag())
//                .put(DESTINATION_RULE_LOAD_BALANCER, CommonUtil.obj2yaml(service.getLoadBalancer()))
                .put(DESTINATION_RULE_EXTRA_SUBSETS, service.getSubsets())
                .put(API_GATEWAY, service.getGateway());

        // host由服务类型决定，
        // 当为动态时，则直接使用服务的后端地址，一般为httpbin.default.svc类似
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
            params.put(DESTINATION_RULE_HOST, decorateHost(service.getCode()));
        }

        //负载均衡相关
        if (service.getLoadBalancer() != null) {
            Service.ServiceLoadBalancer serviceLoadBalancer = service.getLoadBalancer();
            params.put(DESTINATION_RULE_LOAD_BALANCER, DESTINATION_RULE_LOAD_BALANCER);
            params.put(DESTINATION_RULE_LOAD_BALANCER_SIMPLE, serviceLoadBalancer.getSimple());
            if (serviceLoadBalancer.getSlowStartWindow() != null) {
                params.put(DESTINATION_RULE_LOAD_BALANCER_SLOW_START_WINDOW, serviceLoadBalancer.getSlowStartWindow());
            }
            LocalitySettingDTO localitySetting = serviceLoadBalancer.getLocalitySetting();
            if (localitySetting != null && localitySetting.getEnable() != null){
                params.put(DESTINATION_RULE_LOCALITY_ENABLE, String.valueOf(localitySetting.getEnable()));
            }
        }


        if (service.getLoadBalancer() != null && service.getLoadBalancer().getConsistentHash() != null) {
            Service.ServiceLoadBalancer.ConsistentHash consistentHash = service.getLoadBalancer().getConsistentHash();
            params.put(DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH, DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH);
            params.put(DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_HEADER, consistentHash.getHttpHeaderName());
            params.put(DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_SOURCEIP, consistentHash.getUseSourceIp());
        }

        if (service.getLoadBalancer() != null && service.getLoadBalancer().getConsistentHash() != null
                && service.getLoadBalancer().getConsistentHash().getHttpCookie() != null) {
            Service.ServiceLoadBalancer.ConsistentHash.HttpCookie httpCookie = service.getLoadBalancer().getConsistentHash().getHttpCookie();
            params.put(DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE, DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE);
            params.put(DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_NAME, httpCookie.getName());
            params.put(DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_PATH, httpCookie.getPath());
            params.put(DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_TTL, httpCookie.getTtl());
        }

        //连接池相关
        if (service.getConnectionPool() != null) {
            params.put(DESTINATION_RULE_CONNECTION_POOL, CommonUtil.obj2yaml(service.getConnectionPool()));
        }
        if (service.getConnectionPool() != null && service.getConnectionPool().getHttp() != null) {
            PortalServiceConnectionPoolDTO.PortalServiceHttpConnectionPoolDTO portalServiceHttpConnectionPoolDTO
                    = service.getConnectionPool().getHttp();
            params.put(DESTINATION_RULE_HTTP_CONNECTION_POOL, DESTINATION_RULE_HTTP_CONNECTION_POOL);
            params.put(DESTINATION_RULE_HTTP_CONNECTION_POOL_HTTP1MAXPENDINGREQUESTS,
                    portalServiceHttpConnectionPoolDTO.getHttp1MaxPendingRequests());
            params.put(DESTINATION_RULE_HTTP_CONNECTION_POOL_HTTP2MAXREQUESTS,
                    portalServiceHttpConnectionPoolDTO.getHttp2MaxRequests());
            params.put(DESTINATION_RULE_HTTP_CONNECTION_POOL_MAXREQUESTSPERCONNECTION,
                    portalServiceHttpConnectionPoolDTO.getMaxRequestsPerConnection());
            params.put(DESTINATION_RULE_HTTP_CONNECTION_POOL_IDLETIMEOUT,
                    portalServiceHttpConnectionPoolDTO.getIdleTimeout());
        }
        if (service.getConnectionPool() != null && service.getConnectionPool().getTcp() != null) {
            PortalServiceConnectionPoolDTO.PortalServiceTcpConnectionPoolDTO portalServiceTcpConnectionPoolDTO
                    = service.getConnectionPool().getTcp();
            params.put(DESTINATION_RULE_TCP_CONNECTION_POOL, DESTINATION_RULE_TCP_CONNECTION_POOL);
            params.put(DESTINATION_RULE_TCP_CONNECTION_POOL_CONNECT_TIMEOUT,
                    portalServiceTcpConnectionPoolDTO.getConnectTimeout());
            params.put(DESTINATION_RULE_TCP_CONNECTION_POOL_MAX_CONNECTIONS,
                    portalServiceTcpConnectionPoolDTO.getMaxConnections());
        }
        return Arrays.asList(params);
    }
}
