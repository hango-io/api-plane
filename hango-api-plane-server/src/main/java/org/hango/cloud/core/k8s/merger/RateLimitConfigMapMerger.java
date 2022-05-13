package org.hango.cloud.core.k8s.merger;

import org.hango.cloud.meta.ConfigMapRateLimit;
import org.hango.cloud.util.CommonUtil;
import org.hango.cloud.util.function.Equals;
import org.hango.cloud.util.function.Merger;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


/**
 * for ratelimit server config map
 */
public abstract class RateLimitConfigMapMerger implements Merger<ConfigMap> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitConfigMapMerger.class);

    @Override
    public ConfigMap merge(ConfigMap old, ConfigMap latest) {

        if (latest == null || CollectionUtils.isEmpty(latest.getData())) return old;
        if (old == null || CollectionUtils.isEmpty(old.getData())) return latest;

        Map.Entry<String, String> oldConfig = old.getData().entrySet().stream().findFirst().get();
        Map.Entry<String, String> latestConfig = latest.getData().entrySet().stream().findFirst().get();

        // k8s上的非数组，本地渲染的为数组
        ConfigMapRateLimit oldCmrl = str2RateLimitConfig(oldConfig.getValue());
        ConfigMapRateLimit latestCmrl = str2RateLimitConfig(latestConfig.getValue());

        if (oldCmrl == null) return latest;
        if (latestCmrl == null) return old;

        List mergedDescriptors = CommonUtil.mergeList(
                oldCmrl.getDescriptors(), latestCmrl.getDescriptors(), getDescriptorEquals());

        //对descriptors、domain进行覆盖
        oldCmrl.setDescriptors(mergedDescriptors);
        oldCmrl.setDomain(latestCmrl.getDomain());

        String finalConfig = limitConfig2Str(oldCmrl);
        if (!StringUtils.isEmpty(finalConfig)) {
            oldConfig.setValue(finalConfig);
        }
        return old;
    }

    abstract Equals<ConfigMapRateLimit.ConfigMapRateLimitDescriptor> getDescriptorEquals();

    private String limitConfig2Str(ConfigMapRateLimit cmrl) {
        return CommonUtil.obj2yaml(cmrl);
    }

    private ConfigMapRateLimit str2RateLimitConfig(String str) {
        return CommonUtil.yaml2Obj(str, ConfigMapRateLimit.class);
    }
}
