package org.hango.cloud.core.gateway.handler;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.Service;
import org.hango.cloud.meta.dto.PortalServiceConnectionPoolDTO;
import org.hango.cloud.util.CommonUtil;
import org.hango.cloud.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class PortalDestinationRuleServiceDataHandler extends ServiceDataHandler {

    private static final Logger logger = LoggerFactory.getLogger(PortalDestinationRuleServiceDataHandler.class);

    private static YAMLMapper yamlMapper;

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, Service service) {
        TemplateParams params = TemplateParams.instance()
                .put(TemplateConst.DESTINATION_RULE_NAME, service.getCode() + "-" + service.getGateway())
                .put(TemplateConst.DESTINATION_RULE_HOST, service.getBackendService())
                .put(TemplateConst.NAMESPACE, service.getNamespace())
                .put(TemplateConst.API_SERVICE, service.getCode())
                .put(TemplateConst.DESTINATION_RULE_CONSECUTIVE_ERRORS, service.getConsecutiveErrors())
                .put(TemplateConst.DESTINATION_RULE_BASE_EJECTION_TIME, service.getBaseEjectionTime())
                .put(TemplateConst.DESTINATION_RULE_MAX_EJECTION_PERCENT, service.getMaxEjectionPercent())
                .put(TemplateConst.DESTINATION_RULE_MIN_HEALTH_PERCENT, service.getMinHealthPercent())
                .put(TemplateConst.DESTINATION_RULE_PATH, service.getPath())
                .put(TemplateConst.DESTINATION_RULE_TIMEOUT, service.getTimeout())
//                .put(DESTINATION_RULE_LOAD_BALANCER, CommonUtil.obj2yaml(service.getLoadBalancer()))
                .put(TemplateConst.DESTINATION_RULE_EXTRA_SUBSETS, service.getSubsets())
                .put(TemplateConst.API_GATEWAY, service.getGateway());

        // host由服务类型决定，
        // 当为动态时，则直接使用服务的后端地址，一般为httpbin.default.svc类似
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
            params.put(TemplateConst.DESTINATION_RULE_HOST, decorateHost(service.getCode()));
        }

        //负载均衡相关
        if (service.getLoadBalancer() != null) {
            Service.ServiceLoadBalancer serviceLoadBalancer = service.getLoadBalancer();
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER, TemplateConst.DESTINATION_RULE_LOAD_BALANCER);
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_SIMPLE, serviceLoadBalancer.getSimple());
        }

        if (service.getLoadBalancer() != null && service.getLoadBalancer().getConsistentHash() != null) {
            Service.ServiceLoadBalancer.ConsistentHash consistentHash = service.getLoadBalancer().getConsistentHash();
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH, TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH);
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_HEADER, consistentHash.getHttpHeaderName());
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_SOURCEIP, consistentHash.getUseSourceIp());
        }

        if (service.getLoadBalancer() != null && service.getLoadBalancer().getConsistentHash() != null
                && service.getLoadBalancer().getConsistentHash().getHttpCookie() != null) {
            Service.ServiceLoadBalancer.ConsistentHash.HttpCookie httpCookie = service.getLoadBalancer().getConsistentHash().getHttpCookie();
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE, TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE);
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_NAME, httpCookie.getName());
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_PATH, httpCookie.getPath());
            params.put(TemplateConst.DESTINATION_RULE_LOAD_BALANCER_CONSISTENT_HASH_COOKIE_TTL, httpCookie.getTtl());
        }

        //连接池相关
        if (service.getConnectionPool() != null) {
            params.put(TemplateConst.DESTINATION_RULE_CONNECTION_POOL, CommonUtil.obj2yaml(service.getConnectionPool()));
        }
        if (service.getConnectionPool() != null && service.getConnectionPool().getHttp() != null) {
            PortalServiceConnectionPoolDTO.PortalServiceHttpConnectionPoolDTO portalServiceHttpConnectionPoolDTO
                    = service.getConnectionPool().getHttp();
            params.put(TemplateConst.DESTINATION_RULE_HTTP_CONNECTION_POOL, TemplateConst.DESTINATION_RULE_HTTP_CONNECTION_POOL);
            params.put(TemplateConst.DESTINATION_RULE_HTTP_CONNECTION_POOL_HTTP1MAXPENDINGREQUESTS,
                       portalServiceHttpConnectionPoolDTO.getHttp1MaxPendingRequests());
            params.put(TemplateConst.DESTINATION_RULE_HTTP_CONNECTION_POOL_HTTP2MAXREQUESTS,
                       portalServiceHttpConnectionPoolDTO.getHttp2MaxRequests());
            params.put(TemplateConst.DESTINATION_RULE_HTTP_CONNECTION_POOL_MAXREQUESTSPERCONNECTION,
                       portalServiceHttpConnectionPoolDTO.getMaxRequestsPerConnection());
            params.put(TemplateConst.DESTINATION_RULE_HTTP_CONNECTION_POOL_IDLETIMEOUT,
                       portalServiceHttpConnectionPoolDTO.getIdleTimeout());
        }
        if (service.getConnectionPool() != null && service.getConnectionPool().getTcp() != null) {
            PortalServiceConnectionPoolDTO.PortalServiceTcpConnectionPoolDTO portalServiceTcpConnectionPoolDTO
                    = service.getConnectionPool().getTcp();
            params.put(TemplateConst.DESTINATION_RULE_TCP_CONNECTION_POOL, TemplateConst.DESTINATION_RULE_TCP_CONNECTION_POOL);
            params.put(TemplateConst.DESTINATION_RULE_TCP_CONNECTION_POOL_CONNECT_TIMEOUT,
                       portalServiceTcpConnectionPoolDTO.getConnectTimeout());
            params.put(TemplateConst.DESTINATION_RULE_TCP_CONNECTION_POOL_MAX_CONNECTIONS,
                       portalServiceTcpConnectionPoolDTO.getMaxConnections());
        }
        return Arrays.asList(params);
    }
}
