package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 一个服务对应一个gateway,一个gateway里面只配一个server,
 * server里的hosts可以多个
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/31
 **/
@Component
public class GatewayOperator implements k8sResourceOperator<Gateway> {

    @Override
    public Gateway merge(Gateway old, Gateway fresh) {

        List<Server> oldServers = old.getSpec().getServers();
        if (CollectionUtils.isEmpty(oldServers)) {
            throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        }

        Gateway latestGateway = new GatewayBuilder(old).build();

        Server firstOldServer = oldServers.get(0);
        List<String> oldHosts = firstOldServer.getHosts();

        GatewaySpec freshSpec = fresh.getSpec();

        List<Server> freshServers = freshSpec.getServers();
        if (CollectionUtils.isEmpty(freshServers)) {
            return latestGateway;
        }
        Server firstFreshServer = freshServers.get(0);
        List<String> freshHosts = firstFreshServer.getHosts();
        if (CollectionUtils.isEmpty(freshHosts)) {
            return latestGateway;
        }

        Server firstLatestServer = latestGateway.getSpec().getServers().get(0);
        firstLatestServer.setHosts(mergeList(oldHosts, freshHosts, (ot, nt) -> Objects.equals(ot, nt)));
        latestGateway.getSpec().setEnableHttp10(freshSpec.getEnableHttp10());
        latestGateway.getSpec().setDefaultHostForHttp10(freshSpec.getDefaultHostForHttp10());

        firstLatestServer.setCustomIpAddressHeader(firstFreshServer.getCustomIpAddressHeader());
        firstLatestServer.setXffNumTrustedHops(firstFreshServer.getXffNumTrustedHops());
        return latestGateway;
    }

    @Override
    public Gateway subtract(Gateway old, String value) {
        //do nothing
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.Gateway.name().equals(name);
    }

    @Override
    public boolean isUseless(Gateway gateway) {
        return gateway == null ||
                StringUtils.isEmpty(gateway.getApiVersion()) ||
                 gateway.getSpec() == null ||
                  CollectionUtils.isEmpty(gateway.getSpec().getServers());
    }
}
