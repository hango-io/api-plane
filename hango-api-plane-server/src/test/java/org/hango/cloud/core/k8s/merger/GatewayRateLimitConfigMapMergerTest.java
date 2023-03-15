package org.hango.cloud.core.k8s.merger;

import org.hango.cloud.meta.ConfigMapRateLimit;
import org.hango.cloud.util.CommonUtil;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("java:S1192")
public class GatewayRateLimitConfigMapMergerTest {

    @Test
    public void testMerge() {

        RateLimitConfigMapMerger merger = new GatewayRateLimitConfigMapMerger();

        Map<String, String> data1 = buildData("config.yaml",
                "  descriptors:\n" +
                        "  - key: auto-test1-gw1\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw1]-Api[test1]-Id[08638e47-48db\n" +
                        "  - key: auto-test2\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw2]-Api[test2]-Id[08638e47-48db\n" +
                        "  - key: auto-test3\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw3]-Api[test3]-Id[08638e47-48db\n" +
                        "  domain: qingzhou");

        Map<String, String> data2 = buildData("config.yaml",
                "  descriptors:\n" +
                        "  - key: auto-test1-gw2\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 2\n" +
                        "      unit: MINUTE\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw2]-Api[test1]-Id[08638e47-b\n" +
                        "  - key: auto-test4\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw4]-Api[test4]-Id[08638e47-48db\n" +
                        "  - key: auto-test3\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw3]-Api[test3]-Id[08638e47-48db\n" +
                        "  domain: qingzhou\n");


        ConfigMap configMap1 = buildConfigMap(data1);
        ConfigMap configMap2 = buildConfigMap(null);
        ConfigMap configMap3 = buildConfigMap(data2);

        String s = CommonUtil.obj2yaml(configMap1);

        Assert.assertEquals(configMap1, merger.merge(configMap1, null));
        Assert.assertEquals(configMap1, merger.merge(null, configMap1));
        Assert.assertEquals(configMap1, merger.merge(configMap1, configMap2));
        Assert.assertEquals(configMap1, merger.merge(configMap2, configMap1));

        ConfigMap merge1 = merger.merge(configMap1, configMap3);
        String raw = merge1.getData().get("config.yaml");
        ConfigMapRateLimit cmrl = CommonUtil.yaml2Obj(raw, ConfigMapRateLimit.class);

        Assert.assertEquals("qingzhou", cmrl.getDomain());
        Assert.assertEquals(5, cmrl.getDescriptors().size());

        for (ConfigMapRateLimit.ConfigMapRateLimitDescriptor desc : cmrl.getDescriptors()) {
            if (desc.getKey().equals("auto-test1-gw1")) {
                Assert.assertEquals("HOUR", desc.getRateLimit().getUnit());
                Assert.assertEquals(new Integer(1), desc.getRateLimit().getRequestsPerUnit());
            } else if (desc.getKey().equals("auto-test1-gw2")) {
                Assert.assertEquals("MINUTE", desc.getRateLimit().getUnit());
                Assert.assertEquals(new Integer(2), desc.getRateLimit().getRequestsPerUnit());
            } else if (desc.getKey().equals("auto-test2")) {
                Assert.assertEquals("HOUR", desc.getRateLimit().getUnit());
            } else if (desc.getKey().equals("auto-test4")) {
                Assert.assertEquals("HOUR", desc.getRateLimit().getUnit());
                Assert.assertEquals(new Integer(1), desc.getRateLimit().getRequestsPerUnit());
            }
        }
    }

    private ConfigMap buildConfigMap(Map<String, String> data) {
        ConfigMap cm = new ConfigMap();
        cm.setData(data);
        return cm;
    }

    private Map<String, String> buildData(String key, String val) {
        Map<String, String> data = new HashMap<>();
        data.put(key, val);
        return data;
    }
}