package org.hango.cloud.core.k8s.merger;

import org.hango.cloud.meta.ConfigMapRateLimit;
import org.hango.cloud.util.function.Equals;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatewayRateLimitConfigMapMerger extends RateLimitConfigMapMerger {

    @Override
    Equals<ConfigMapRateLimit.ConfigMapRateLimitDescriptor> getDescriptorEquals() {

        return (ot, nt) -> {
            String oldVal = ot.getValue();
            String newVal = nt.getValue();

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
        };
    }
}
