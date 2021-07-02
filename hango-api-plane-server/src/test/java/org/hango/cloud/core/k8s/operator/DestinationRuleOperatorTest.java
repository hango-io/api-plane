package org.hango.cloud.core.k8s.operator;


import com.google.common.collect.ImmutableMap;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleSpec;
import me.snowdrop.istio.api.networking.v1alpha3.Subset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DestinationRuleOperatorTest {

    DestinationRuleOperator operator;

    @Before
    public void init() {
        operator = new DestinationRuleOperator();
    }


    @Test
    public void testMerge() {

        Map<String, String> labels = ImmutableMap.of("a", "b");

        Subset s1 = getSubset("s1", null);
        Subset s2 = getSubset("s2", null);
        Subset s3 = getSubset("s3", null);

        DestinationRuleSpec spec = getDestinationRuleSpec(Arrays.asList(s1,s2,s3));
        DestinationRule old = getDestinationRule("v1", "destinationRule", spec);

        Subset fresh1 = getSubset("s1", null);
        Subset fresh4 = getSubset("s4", null);

        DestinationRuleSpec freshSpec = getDestinationRuleSpec(Arrays.asList(fresh1, fresh4));
        DestinationRule fresh = getDestinationRule("v1", "destinationRule", freshSpec);

        DestinationRule destinationRule = operator.merge(old, fresh);
        List<Subset> subsets = destinationRule.getSpec().getSubsets();
        Assert.assertTrue(subsets.size() == 4);
//        subsets.stream().forEach(ss -> {
//            if (ss.getName().equals("s1")) {
//                Assert.assertEquals(ss.getGwLabels(), getGwLabels("gw11"));
//            } else if (ss.getName().equals("s4")) {
//                Assert.assertEquals(ss.getGwLabels(), getGwLabels("gw4"));
//            }
//        });
    }

    @Test
    public void testSubtract() {

        Subset s1 = getSubset("s1", null);
        Subset s2 = getSubset("s2", null);

        DestinationRuleSpec spec = getDestinationRuleSpec(Arrays.asList(s1,s2));
        DestinationRule old = getDestinationRule("v1", "destinationRule", spec);

        DestinationRule result = operator.subtract(old, "s1");
        Assert.assertTrue(result.getSpec().getSubsets().size() == 1);
        Assert.assertTrue(result.getSpec().getSubsets().get(0).getName().equals("s2"));
//        Assert.assertTrue(result.getSpec().getSubsets().get(0).getGwLabels().equals(getGwLabels("gw2")));
    }


    private static DestinationRule getDestinationRule(String apiVersion, String kind, DestinationRuleSpec spec) {
        DestinationRule ds = new DestinationRule();
        ds.setApiVersion(apiVersion);
        ds.setKind(kind);
        ds.setSpec(spec);
        return ds;
    }

    private static Subset getSubset(String name, Map<String, String> labels) {
        Subset ss = new Subset();
        ss.setName(name);
        ss.setLabels(labels);
        return ss;
    }

    private static DestinationRuleSpec getDestinationRuleSpec(List<Subset> subsets) {
        DestinationRuleSpec drs = new DestinationRuleSpec();
        drs.setSubsets(subsets);
        return drs;
    }

    private static Map<String, String> getGwLabels(String gw) {
        return ImmutableMap.of("gw_cluster", gw);
    }
}
