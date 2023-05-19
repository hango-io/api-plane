package org.hango.cloud.core.k8s.merger;

import org.hango.cloud.util.CommonUtil;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiterBuilder;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("java:S1192")
public class SmartLimiterMergerTest {

    @Test
    public void testMerge() {

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
                "        unit: 1\n" +
                "        value: Service[a.default]-User[none]-Gateway[null]-Api[3]-Id[cc41fdbd-5994-46ef-9f8d-213136ce4d88]\n" +
                "      - key: header_match\n" +
                "        unit: 3\n" +
                "        value: Service[a.default]-User[none]-Gateway[null]-Api[3]-Id[bfbd4dd4-a373-427d-949c-ae7c97aa74a0]\n" +
                "      - key: header_match\n" +
                "        rate_limit:\n" +
                "        unit: 1\n" +
                "        value: Service[a.default]-User[none]-Gateway[null]-Api[3]-Id[849252b3-d6c2-4306-9611-e95071f24fd5]\n" +
                "      - key: header_match\n" +
                "        unit: 3\n" +
                "        value: Service[a.default]-User[none]-Gateway[null]-Api[3]-Id[5db406cf-943b-477a-81ab-a7fc63b567fe]";

        String yaml2 = "apiVersion: microservice.netease.com/v1alpha1\n" +
                "kind: SmartLimiter\n" +
                "metadata:\n" +
                "  name: a\n" +
                "  namespace: powerful-v13\n" +
                "spec:\n" +
                "  ratelimitConfig:\n" +
                "    rate_limit_conf:\n" +
                "      descriptors:\n" +
                "      - key: header_match\n" +
                "        unit: 1\n" +
                "        value: Service[b.default]-User[none]-Gateway[null]-Api[3]-Id[cc41fdbd-5994-46ef-9f8d-213136ce4d88]\n" +
                "      - key: header_match\n" +
                "        unit: 3\n" +
                "        value: Service[b.default]-User[none]-Gateway[null]-Api[3]-Id[bfbd4dd4-a373-427d-949c-ae7c97aa74a0]\n";

        SmartLimiter sl1 = CommonUtil.yaml2Obj(yaml1, SmartLimiter.class);
        SmartLimiter sl2 = CommonUtil.yaml2Obj(yaml2, SmartLimiter.class);
        SmartLimiter sl1_1 = CommonUtil.yaml2Obj(yaml1, SmartLimiter.class);
        SmartLimiter sl2_1 = CommonUtil.yaml2Obj(yaml2, SmartLimiter.class);
        SmartLimiter sl1_2 = CommonUtil.yaml2Obj(yaml1, SmartLimiter.class);


        SmartLimiter emptySl = new SmartLimiterBuilder().build();

        SmartLimiterMerger merger = new SmartLimiterMerger();
        SmartLimiter merged1 = merger.merge(sl1, sl2);
        Assert.assertEquals(2, merged1.getSpec().getRatelimitConfig().getRateLimitConf().getDescriptors().size());

        SmartLimiter merged2 = merger.merge(sl2_1, sl1_1);
        Assert.assertEquals(4, merged2.getSpec().getRatelimitConfig().getRateLimitConf().getDescriptors().size());

        SmartLimiter merged3 = merger.merge(sl1_2, emptySl);
        Assert.assertNull(merged3.getSpec());

        SmartLimiter merged4 = merger.merge(emptySl, sl1);
        Assert.assertSame(sl1, merged4);

    }
}
