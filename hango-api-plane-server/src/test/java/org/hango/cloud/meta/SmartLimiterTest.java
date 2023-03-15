package org.hango.cloud.meta;

import org.hango.cloud.util.CommonUtil;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import org.junit.Test;


public class SmartLimiterTest {

    @Test
    public void testTrans() {

        String yaml = "apiVersion: microservice.netease.com/v1alpha1\n" +
                "kind: SmartLimiter\n" +
                "metadata:\n" +
                "  name: a.default\n" +
                "  namespace: default\n" +
                "spec:\n" +
                "  ratelimitConfig:\n" +
                "    descriptors:\n" +
                "    - domain: \"qingzhou\"\n" +
                "      descriptors:\n" +
                "      - key: \"header_match\"\n" +
                "        value: \"Service[a.default]-User[none]-Gateway[null]-Api[null]-Id[6598b3cf-a16c-49ff-8a7e-a586684d75c1]\"\n" +
                "        rate_limit:\n" +
                "          unit: \"HOUR\"\n" +
                "          requests_per_unit: 1";

        SmartLimiter smartLimiter = CommonUtil.yaml2Obj(yaml, SmartLimiter.class);
    }
}
