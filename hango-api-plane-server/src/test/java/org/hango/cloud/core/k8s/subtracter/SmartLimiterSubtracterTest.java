package org.hango.cloud.core.k8s.subtracter;


import org.hango.cloud.util.CommonUtil;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import org.junit.Assert;
import org.junit.Test;

public class SmartLimiterSubtracterTest {

    @Test
    public void subtract() {

        String yaml1 = "apiVersion: microservice.netease.com/v1alpha1\n" +
                "kind: SmartLimiter\n" +
                "metadata:\n" +
                "  name: a\n" +
                "  namespace: powerful-v13\n" +
                "spec:\n" +
                "  ratelimitConfig:\n" +
                "    rate_limit_conf:\n" +
                "      descriptors:\n" +
                "      - key: header_match\n" +
                "        rate_limit:\n" +
                "          requests_per_unit: 2\n" +
                "          unit: SECOND\n" +
                "        value: Service[b.default]-User[none]-Gateway[null]-Api[3]-Id[cc41fdbd-5994-46ef-9f8d-213136ce4d88]\n" +
                "      - key: header_match\n" +
                "        rate_limit:\n" +
                "          requests_per_unit: 2\n" +
                "          unit: HOUR\n" +
                "        value: Service[b.default]-User[none]-Gateway[null]-Api[3]-Id[bfbd4dd4-a373-427d-949c-ae7c97aa74a0]\n";

        SmartLimiter sl = CommonUtil.yaml2Obj(yaml1, SmartLimiter.class);

        SmartLimiterSubtracter subtracter = new SmartLimiterSubtracter();
        SmartLimiter subtracted = subtracter.subtract(sl);

        Assert.assertNull(subtracted.getSpec());
    }
}
