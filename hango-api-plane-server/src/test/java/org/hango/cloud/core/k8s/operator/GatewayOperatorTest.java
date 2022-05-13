package org.hango.cloud.core.k8s.operator;


import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GatewayOperatorTest {

    GatewayOperator operator;

    @Before
    public void init() {
        operator = new GatewayOperator();
    }

    @Test
    public void testMerge() {

        Gateway old = getGateway(getGatewaySpec(Arrays.asList(getServer(Arrays.asList("a", "b"),"test1",2)),false));
        Gateway fresh = getGateway(getGatewaySpec(Arrays.asList(getServer(Arrays.asList("b", "c", "d"), StringUtils.EMPTY,1)),true));

        Gateway merge = operator.merge(old, fresh);
        Assert.assertTrue(merge.getSpec().getServers().get(0).getHosts().size() == 4);
        Assert.assertEquals(merge.getSpec().getEnableHttp10(), true);
        Assert.assertEquals(merge.getSpec().getServers().get(0).getCustomIpAddressHeader(), StringUtils.EMPTY);
        Assert.assertTrue(merge.getSpec().getServers().get(0).getXffNumTrustedHops() == 1);
    }

    private static Server getServer(List<String> hosts,String customIpAddressHeader,int xffNumTrustedHops) {
        Server server = new Server();
        server.setHosts(hosts);
        server.setCustomIpAddressHeader(customIpAddressHeader);
        server.setXffNumTrustedHops(xffNumTrustedHops);
        return server;
    }

    private static GatewaySpec getGatewaySpec(List<Server> servers,Boolean enableHttp10) {
        GatewaySpec spec = new GatewaySpec();
        spec.setServers(servers);
        spec.setEnableHttp10(enableHttp10);
        return spec;
    }

    private static Gateway getGateway(GatewaySpec spec) {
        Gateway gateway = new Gateway();
        gateway.setSpec(spec);
        return gateway;
    }
}
