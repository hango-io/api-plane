package org.hango.cloud.core.k8s.operator;

import com.google.common.collect.ImmutableMap;
import me.snowdrop.istio.slime.v1alpha1.Plugin;
import me.snowdrop.istio.slime.v1alpha1.PluginManager;
import me.snowdrop.istio.slime.v1alpha1.PluginManagerSpec;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class PluginManagerOperatorTest {

    PluginManagerOperator operator;

    @Before
    public void init() {
        operator = new PluginManagerOperator();
    }

    @Test
    public void testMerge() {

        PluginManager pm1 = getPluginManager(
                getPluginManagerSpec(
                        Arrays.asList(getPlugin("pa", true)),
                        ImmutableMap.of("gw","gw11")));

        PluginManager pm2 = getPluginManager(
                getPluginManagerSpec(
                        Arrays.asList(getPlugin("pa", false), getPlugin("pb", true)),
                        ImmutableMap.of("gw","gw12")));

        PluginManager merge = operator.merge(pm1, pm2);

        PluginManagerSpec mergeSpec = merge.getSpec();

        assertTrue(mergeSpec.getWorkloadLabels().get("gw").equals("gw12"));
        assertTrue(mergeSpec.getPlugin().size() == 2);
        for (Plugin p : mergeSpec.getPlugin()) {
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

        PluginManager pm1 = getPluginManager(
                getPluginManagerSpec(
                        Arrays.asList(getPlugin("pa", true)),
                        ImmutableMap.of("gw","gw11")));

        operator.subtract(pm1, "1");
        assertTrue(operator.isUseless(pm1));
    }

    private PluginManager getPluginManager(PluginManagerSpec spec) {
        PluginManager pm = new PluginManager();
        pm.setSpec(spec);
        return pm;
    }

    private PluginManagerSpec getPluginManagerSpec(List<Plugin> plugins, Map<String, String> workloads) {
        PluginManagerSpec pms = new PluginManagerSpec();
        pms.setPlugin(plugins);
        pms.setWorkloadLabels(workloads);
        return pms;
    }

    private Plugin getPlugin(String name, boolean enable) {
        Plugin plugin = new Plugin();
        plugin.setName(name);
        plugin.setEnable(enable);
        return plugin;
    }
}
