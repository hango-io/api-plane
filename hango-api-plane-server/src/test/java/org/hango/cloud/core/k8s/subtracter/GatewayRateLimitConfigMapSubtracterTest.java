package org.hango.cloud.core.k8s.subtracter;

import org.hango.cloud.meta.ConfigMapRateLimit;
import org.hango.cloud.util.CommonUtil;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GatewayRateLimitConfigMapSubtracterTest {

    @Test
    public void subtract() {

        Map<String, String> data1 = buildData("config.yaml",
                "  descriptors:\n" +
                        "  - key: auto-test1\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw1]-Api[test1]-Id[08638e47-48db\n" +
                        "  - key: auto-test1\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: MINUTE\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw1]-Api[test1]-Id[08638e47-\n" +
                        "  - key: auto-test3\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw1]-Api[test3]-Id[08638e47-48db\n" +
                        "  - key: auto-test1\n" +
                        "    rate_limit:\n" +
                        "      requests_per_unit: 1\n" +
                        "      unit: MINUTE\n" +
                        "    value: Service[httpbin]-User[none]-Gateway[gw2]-Api[test1]-Id[08638e47-\n" +
                        "  domain: qingzhou");


        RateLimitConfigMapSubtracter subtracter = new GatewayRateLimitConfigMapSubtracter("gw1", "test1");
        ConfigMap configMap1 = buildConfigMap(data1);
        ConfigMap subtracted = subtracter.subtract(configMap1);

        String rawData = subtracted.getData().get("config.yaml");
        ConfigMapRateLimit rateLimitConfig = CommonUtil.yaml2Obj(rawData, ConfigMapRateLimit.class);

        Assert.assertEquals(2, rateLimitConfig.getDescriptors().size());
        ConfigMapRateLimit.ConfigMapRateLimitDescriptor oneRateLimit = rateLimitConfig.getDescriptors().get(0);
        Assert.assertEquals("auto-test3", oneRateLimit.getKey());
        Assert.assertEquals("HOUR", oneRateLimit.getRateLimit().getUnit());
        Assert.assertEquals(new Integer(1), oneRateLimit.getRateLimit().getRequestsPerUnit());
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