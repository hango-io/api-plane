package org.hango.cloud.core.k8s.operator;

import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@SuppressWarnings("java:S1192")
public class SharedConfigTest {

    SharedConfigOperator operator;

    @Before
    public void init() {
        operator = new SharedConfigOperator();
    }

    @Test
    public void merge() {

        RateLimitConfig config1 = getRateLimitConfig("qz",
                Arrays.asList(
                        getRateLimitDescriptor("sa-api1",
                                getRateLimit(10, null), "Service[autotest]-User[none]-Gateway[gw1-sa]-Api[sa-api1]-Id[36dfa"),
                        getRateLimitDescriptor("sa-api2",
                                getRateLimit(2, null),  "Service[autotest]-User[none]-Gateway[gw2-sa]-Api[sa-api2]-Id[36dfa")));

        RateLimitConfig config2 = getRateLimitConfig("qz1",
                Arrays.asList(
                        getRateLimitDescriptor("sa-api5",
                                getRateLimit(0, null), "Service[autotest]-User[none]-Api[sa-api5]-Id[36dfa")));
        SharedConfig sc = getSharedConfig(getSharedConfigSpec(Arrays.asList(config1, config2)));


        RateLimitConfig config1_1 = getRateLimitConfig("qz", Arrays.asList(
                getRateLimitDescriptor("sa-api1", getRateLimit(5, null), "Service[autotest]-User[none]-Gateway[gw1-sa]-Api[sa-api1]-Id[36dfa"),
                getRateLimitDescriptor("sa-api3", getRateLimit(1, null), "Service[autotest]-User[none]-Gateway[gw3-sa]-Api[sa-api3]-Id[36dfa")));
        RateLimitConfig config1_2 = getRateLimitConfig("qz1", Arrays.asList(
                getRateLimitDescriptor("sb-api3", getRateLimit(2, null),"Service[autotest]-User[none]-Gateway[gw3-sb]-Api[sb-api3]-Id[36dfa")));
        RateLimitConfig config1_3 = getRateLimitConfig("qz2", Arrays.asList(
                getRateLimitDescriptor("sb-api3", getRateLimit(3, null),"Service[autotest]-User[none]-Gateway[gw3-sb]-Api[sb-api3]-Id[36dfa")));
        SharedConfig sc1 = getSharedConfig(getSharedConfigSpec(Arrays.asList(config1_1, config1_2, config1_3)));

        SharedConfig result = operator.merge(sc, sc1);

        Assert.assertTrue(result.getSpec().getRateLimitConfigs().size() == 3);
        for (RateLimitConfig rateLimitConfig : result.getSpec().getRateLimitConfigs()) {
            if (rateLimitConfig.getDomain().equals("qz")) {
                Assert.assertTrue(rateLimitConfig.getDescriptors().size() == 3);
            } else if (rateLimitConfig.getDomain().equals("qz1")) {
                Assert.assertTrue(rateLimitConfig.getDescriptors().size() == 2);
                for (RateLimitDescriptor rateLimitDescriptor : rateLimitConfig.getDescriptors()) {
                    if (rateLimitDescriptor.getApi().equals("sa-api1")) {
                        Assert.assertTrue(rateLimitDescriptor.getRateLimit().getRequestsPerUnit() == 5);
                    }
                }

            } else if (rateLimitConfig.getDomain().equals("qz2")) {
                Assert.assertTrue(rateLimitConfig.getDescriptors().size() == 1);
            }
        }

        RateLimitDescriptor d3_1 = getRateLimitDescriptor("sa-api1", getRateLimit(10, "MINUTE"), "Service[autotest]-User[none]-Gateway[gw1-sa]-Api[sa-api1]-Id[36dfa");
        RateLimitDescriptor d3_2 = getRateLimitDescriptor("sa-api1", getRateLimit(5, "HOUR"), "Service[autotest]-User[none]-Gateway[gw1-sa]-Api[sa-api1]-Id[36dfa");
        RateLimitConfig config3 = getRateLimitConfig("qz", Arrays.asList(d3_1, d3_2));
        SharedConfig sc3 = getSharedConfig(getSharedConfigSpec(Arrays.asList(config3)));

        SharedConfig sc3_ = new SharedConfigBuilder(sc3).build();
        List<RateLimitDescriptor> descriptors = sc3_.getSpec().getRateLimitConfigs().get(0).getDescriptors();
        descriptors.get(0).getRateLimit().setUnit("HOUR");
        descriptors.get(0).getRateLimit().setRequestsPerUnit(1);
        descriptors.get(1).getRateLimit().setUnit("MINUTE");
        descriptors.get(1).getRateLimit().setRequestsPerUnit(100);
        RateLimitDescriptor nd3 = getRateLimitDescriptor("sa", getRateLimit(99, "OOO"), "Service[autotest]-User[none]-Gateway[gw-sa]-Api[sa]-Id[36dfa");
        descriptors.add(0, nd3);

        RateLimitConfig nc3 = getRateLimitConfig("q1z", Collections.emptyList());
        sc3_.getSpec().getRateLimitConfigs().add(nc3);

        SharedConfig result1 = operator.merge(sc3, sc3_);
        Assert.assertTrue(result1.getSpec().getRateLimitConfigs().size() == 2);

        result1.getSpec().getRateLimitConfigs().forEach(c -> {
            if (c.getDomain().equals("qz")) {
                Assert.assertTrue(c.getDescriptors().size() == 3);
            }
        });

    }

    @Test
    public void subtract() {

        RateLimitConfig config1 = getRateLimitConfig("qz",
                Arrays.asList(
                        getRateLimitDescriptor("sa-api1",
                                getRateLimit(1, null), "Service[autotest]-User[none]-Gateway[gw1-sa]-Api[sa-api1]-Id[36dfa"),
                        getRateLimitDescriptor("sa-api2",
                                getRateLimit(2, null), "Service[autotest]-User[none]-Gateway[gw2-sa]-Api[sa-api2]-Id[36dfa")));

        RateLimitConfig config2 = getRateLimitConfig("qz1",
                Arrays.asList(
                        getRateLimitDescriptor("sb-api1",
                                getRateLimit(3, null), "Service[autotest]-User[none]-Gateway[gw1-sb]-Api[sb-api1]-Id[36dfa"),
                        getRateLimitDescriptor("sa-api1",
                                getRateLimit(4, null), "Service[autotest]-User[none]-Gateway[gw1-sa]-Api[sa-api1]-Id[36dfa")));

        SharedConfig sc = getSharedConfig(getSharedConfigSpec(Arrays.asList(config1, config2)));

        SharedConfig result = operator.subtract(sc, "sa-api1");
        Assert.assertTrue(result.getSpec().getRateLimitConfigs().get(0).getDescriptors().size() == 1);
        Assert.assertTrue(result.getSpec().getRateLimitConfigs().get(1).getDescriptors().size() == 1);
    }

    private static RateLimit getRateLimit(int perUnit, Object unit) {
        RateLimit l = new RateLimit();
        l.setRequestsPerUnit(perUnit);
        l.setUnit(unit);
        return l;
    }

    private static RateLimitDescriptor getRateLimitDescriptor(String api, RateLimit ratelimit, String value) {
        RateLimitDescriptor d = new RateLimitDescriptor();
        d.setApi(api);
        d.setRateLimit(ratelimit);
        d.setValue(value);
        return d;
    }

    private static RateLimitConfig getRateLimitConfig(String domain, List<RateLimitDescriptor> descriptors) {
        RateLimitConfig c = new RateLimitConfig();
        c.setDomain(domain);
        c.setDescriptors(descriptors);
        return c;
    }

    private static SharedConfigSpec getSharedConfigSpec(List<RateLimitConfig> configs) {
        SharedConfigSpec spec = new SharedConfigSpec();
        spec.setRateLimitConfigs(configs);
        return spec;
    }

    private static SharedConfig getSharedConfig(SharedConfigSpec spec) {
        SharedConfig sc = new SharedConfig();
        sc.setSpec(spec);
        return sc;
    }
}
