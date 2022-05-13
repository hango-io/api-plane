package org.hango.cloud.core.k8s.operator;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.k8s.K8sTypes;
import org.junit.Before;
import org.junit.Test;
import slime.microservice.plugin.v1alpha1.PluginManagerOuterClass;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/13
 **/
public class PluginManagerOperatorTest {

    PluginManagerOperator operator;

    @Before
    public void init() {
        operator = new PluginManagerOperator();
    }

    @Test
    public void testMerge() {

        K8sTypes.PluginManager pm1 = getPluginManager(
                        Arrays.asList(getPlugin("pa", true)),
                        ImmutableMap.of("gw","gw11"));

        K8sTypes.PluginManager pm2 = getPluginManager(
                        Arrays.asList(getPlugin("pa", false), getPlugin("pb", true)),
                        ImmutableMap.of("gw","gw12"));

        K8sTypes.PluginManager merge = operator.merge(pm1, pm2);

        PluginManagerOuterClass.PluginManager spec = merge.getSpec();

        assertTrue(spec.getWorkloadLabelsMap().get("gw").equals("gw12"));
        assertTrue(spec.getPluginCount() == 2);
        for (PluginManagerOuterClass.Plugin p : spec.getPluginList()) {
            if (p.getName().equals("pa")) {
                assertTrue(!p.getEnable());
            } else if (p.getName().equals("pb")) {
                assertTrue(p.getEnable());
            } else {
                assertTrue(false);
            }
        }

    }

    @Test
    public void testSubtract() {

        K8sTypes.PluginManager pm1 = getPluginManager(
                        Arrays.asList(getPlugin("pa", true)),
                        ImmutableMap.of("gw","gw11"));

        operator.subtract(pm1, "1");
        assertTrue(operator.isUseless(pm1));
    }


    private K8sTypes.PluginManager getPluginManager(List<PluginManagerOuterClass.Plugin> plugins, Map<String, String> workloads) {
        PluginManagerOuterClass.PluginManager build = PluginManagerOuterClass.PluginManager.newBuilder()
                .addAllPlugin(plugins).putAllWorkloadLabels(workloads).build();
        K8sTypes.PluginManager pluginManager = new K8sTypes.PluginManager();
        pluginManager.setSpec(build);
        return pluginManager;
    }

    private PluginManagerOuterClass.Plugin getPlugin(String name, boolean enable) {
        return PluginManagerOuterClass.Plugin.newBuilder().setEnable(enable).setName(name).build();
    }
}
