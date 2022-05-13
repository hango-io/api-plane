package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.editor.PathExpressionEnum;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/28
 **/
public class SharedConfigOperator implements k8sResourceOperator<SharedConfig> {

    @Override
    public SharedConfig merge(SharedConfig old, SharedConfig fresh) {
        SharedConfigSpec oldSpec = old.getSpec();
        SharedConfigSpec freshSpec = fresh.getSpec();

        SharedConfig latest = new SharedConfigBuilder(old).build();
        SharedConfigSpec latestSpec = latest.getSpec();
        List<RateLimitConfig> latestConfigs = latestSpec.getRateLimitConfigs();

        List<RateLimitConfig> oldConfigs = oldSpec.getRateLimitConfigs();
        List<RateLimitConfig> freshConfigs = freshSpec.getRateLimitConfigs();

        if (CollectionUtils.isEmpty(oldConfigs)) {
            latestSpec.setRateLimitConfigs(freshConfigs);
            return latest;
        }

        for (RateLimitConfig latestConfig : latestConfigs) {
            for (RateLimitConfig freshConfig : freshConfigs) {
                if (latestConfig.getDomain().equals(freshConfig.getDomain())) {
                    latestConfig.setDescriptors(mergeList(latestConfig.getDescriptors(), freshConfig.getDescriptors(), new RateLimitDescriptorEquals()));
                }
            }
        }

        latestSpec.setRateLimitConfigs(mergeList(freshConfigs, latestConfigs, new RateLimitConfigEquals()));
        return latest;
    }

    private class RateLimitConfigEquals implements Equals<RateLimitConfig> {
        @Override
        public boolean apply(RateLimitConfig or, RateLimitConfig nr) {
            return Objects.equals(or.getDomain(), nr.getDomain());
        }
    }

    private class RateLimitDescriptorEquals implements Equals<RateLimitDescriptor> {
        @Override
        public boolean apply(RateLimitDescriptor or, RateLimitDescriptor nr) {

            String oldVal = or.getValue();
            String newVal = nr.getValue();

            //eg. Service[httpbin]-User[none]-Gateway[gw]-Api[httpbin]-Id[08638e47-48db-43bc-9c21-07ef892b5494]
            // 当Api[]和Gateway[]中的值分别相等时，才认为两者相当
            Pattern pattern = Pattern.compile("(Service.*)-(User.*)-(Gateway.*)-(Api.*)-(Id.*)");
            Matcher oldMatcher = pattern.matcher(oldVal);
            Matcher newMatcher = pattern.matcher(newVal);
            if (oldMatcher.find() && newMatcher.find()) {
                return Objects.equals(oldMatcher.group(3), newMatcher.group(3)) &&
                        Objects.equals(oldMatcher.group(4), newMatcher.group(4));
            }
            return false;
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.SharedConfig.name().equals(name);
    }

    @Override
    public boolean isUseless(SharedConfig sharedConfig) {
        return sharedConfig == null ||
                StringUtils.isEmpty(sharedConfig.getApiVersion()) ||
                 sharedConfig.getSpec() == null ||
                  CollectionUtils.isEmpty(sharedConfig.getSpec().getRateLimitConfigs());
    }

    @Override
    public SharedConfig subtract(SharedConfig old, String value) {
        ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_SC_RATELIMITDESC.translate(value));
        return gen.object(SharedConfig.class);
    }
}
