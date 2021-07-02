package org.hango.cloud.core.k8s.operator;

import me.snowdrop.istio.slime.v1alpha1.EnvoyPlugin;
import me.snowdrop.istio.slime.v1alpha1.EnvoyPluginSpec;
import me.snowdrop.istio.slime.v1alpha1.Plugin;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        EnvoyPlugin gp1 = getGatewayPlugin(getGatewayPluginSpec(Arrays.asList("gw-1", "gw-2"),
                                                                Arrays.asList("host1", "host2"),
                                                                Arrays.asList(getPlugins("p1", Collections.emptyMap())),
                                                                Arrays.asList("route1", "route2"),
                                                                Arrays.asList("service1", "service2")));

        EnvoyPlugin gp2 = getGatewayPlugin(getGatewayPluginSpec(Arrays.asList("gw-3"),
                Arrays.asList("host3", "host4"),
                Arrays.asList(getPlugins("p2", Collections.emptyMap())),
                Arrays.asList("route3"),
                Arrays.asList("service3")));

        EnvoyPlugin merge = operator.merge(gp1, gp2);

        EnvoyPluginSpec spec = merge.getSpec();
        assertEquals(1, spec.getGateway().size());
        assertEquals("gw-3", spec.getGateway().get(0));
        assertEquals(2, spec.getHost().size());
        assertTrue(spec.getHost().contains("host3") && spec.getHost().contains("host4"));
        assertEquals(1, spec.getPlugins().size());
        assertEquals("p2", spec.getPlugins().get(0).getName());
        assertEquals(1, spec.getRoute().size());
        assertEquals("route3", spec.getRoute().get(0));
        assertEquals("service3", spec.getService().get(0));
    }


    private static EnvoyPlugin getGatewayPlugin(EnvoyPluginSpec spec) {
        EnvoyPlugin gp = new EnvoyPlugin();
        gp.setSpec(spec);
        return gp;
    }

    private static EnvoyPluginSpec getGatewayPluginSpec(List<String> gateway, List<String> hosts, List<Plugin> plugins,
                                                          List<String> routes, List<String> service) {
        EnvoyPluginSpec spec = new EnvoyPluginSpec();
        spec.setGateway(gateway);
        spec.setHost(hosts);
        spec.setPlugins(plugins);
        spec.setRoute(routes);
        spec.setService(service);
        return spec;
    }

    private static Plugin getPlugins(String name, Map<String, Object> settings) {
        Plugin plugins = new Plugin();
        plugins.setName(name);
        plugins.setSettings(settings);
        return plugins;
    }
}
