package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.k8s.K8sTypes;
import org.junit.Before;
import org.junit.Test;
import slime.microservice.plugin.v1alpha1.EnvoyPluginOuterClass;
import slime.microservice.plugin.v1alpha1.PluginManagerOuterClass;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GatewayPluginOperatorTest {

    GatewayPluginOperator operator;

    @Before
    public void init() {
        operator = new GatewayPluginOperator();
    }

    @Test
    public void merge() {

        K8sTypes.EnvoyPlugin gp1 = getGatewayPlugin(Arrays.asList("gw-1", "gw-2"),
                Arrays.asList("host1", "host2"),
                Arrays.asList(getPlugins("p1")),
                Arrays.asList("route1", "route2"),
                Arrays.asList("service1", "service2"));

        K8sTypes.EnvoyPlugin gp2 = getGatewayPlugin(Arrays.asList("gw-3"),
                Arrays.asList("host3", "host4"),
                Arrays.asList(getPlugins("p2")),
                Arrays.asList("route3"),
                Arrays.asList("service3"));

        K8sTypes.EnvoyPlugin merge = operator.merge(gp1, gp2);

        EnvoyPluginOuterClass.EnvoyPlugin spec = merge.getSpec();
        assertEquals(1, spec.getGatewayCount());
        assertEquals("gw-3", spec.getGateway(0));
        assertEquals(2, spec.getHostCount());
        assertTrue(spec.getHostList().contains("host3") && spec.getHostList().contains("host4"));
        assertEquals(1, spec.getPluginsCount());
        assertEquals("p2", spec.getPlugins(0).getName());
        assertEquals(1, spec.getRouteCount());
        assertEquals("route3", spec.getRoute(0));
        assertEquals("service3", spec.getService(0));
    }


    private static K8sTypes.EnvoyPlugin getGatewayPlugin(List<String> gateway, List<String> hosts, List<PluginManagerOuterClass.Plugin> plugins,
                                                          List<String> routes, List<String> service) {
        K8sTypes.EnvoyPlugin spec = new K8sTypes.EnvoyPlugin();
        EnvoyPluginOuterClass.EnvoyPlugin.Builder builder = EnvoyPluginOuterClass.EnvoyPlugin.newBuilder()
                .addAllGateway(gateway)
                .addAllHost(hosts)
                .addAllPlugins(plugins)
                .addAllRoute(routes)
                .addAllService(service);
        spec.setSpec(builder.build());
        return spec;
    }

    private static PluginManagerOuterClass.Plugin getPlugins(String name) {
        return PluginManagerOuterClass.Plugin.newBuilder().setName(name).build();
    }
}
