package org.hango.cloud.mcp;

import org.hango.cloud.ApiPlaneApplication;
import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourceGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ratelimit.config.Config;

/**
 *
 * @date 2020/5/8
 **/
@SpringBootTest(classes = ApiPlaneApplication.class, properties = {"k8s.clusters.default.k8s-api-server=https://1.1.1.1", "nonK8sMode=true"})
public class RlsClusterClientTest extends BaseTest {
    @Autowired
    McpMarshaller marshaller;

    @Test
    public void marshallTest() {
        String config = "{\"apiVersion\":\"v1\",\"kind\":\"ConfigMap\",\"metadata\":{\"labels\":{\"skiff-api-plane-version\":\"release-1.2\",\"skiff-api-plane-type\":\"api-plane\"},\"name\":\"rate-limit-config\",\"namespace\":\"gateway-system\"},\"data\":{\"config.yaml\":\"descriptors:\\n- key: generic_key\\n  rate_limit:\\n    requests_per_unit: 2\\n    unit: HOUR\\n  value: Service[qz]-User[none]-Gateway[gateway-proxy-2]-Api[300]-Id[hash:-1142466717]\\ndomain: qingzhou\\n\"}}";
        ratelimit.config.Config.RateLimitConf.Builder builder = ratelimit.config.Config.RateLimitConf.newBuilder();
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(config);
        String yaml = gen.getValue("$.data.['config.yaml']");
        String json = ResourceGenerator.yaml2json(yaml);
        marshaller.merge(json, builder);
        ratelimit.config.Config.RateLimitConf conf = builder.build();
        ratelimit.config.Config.RateLimitConf conf2 = ratelimit.config.Config.RateLimitConf
                .newBuilder().setDomain("qingzhou")
                .addDescriptors(Config.RateLimitDescriptorConfig.newBuilder().setKey("generic_key").setValue("Service[qz]-User[none]-Gateway[gateway-proxy-2]-Api[300]-Id[hash:-1142466717]").setRateLimit(Config.RateConfig.newBuilder().setRequestsPerUnit(2).setUnit(Config.RateConfig.UnitType.HOUR).build()).build())
                .build();
        Assert.assertEquals(conf, conf2);
    }
}
