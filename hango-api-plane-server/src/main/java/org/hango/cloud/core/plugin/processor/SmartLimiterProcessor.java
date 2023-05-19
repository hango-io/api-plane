package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hango.cloud.util.constant.PluginConstant.CLUSTER_GROUP_LIMITER;
import static org.hango.cloud.util.constant.PluginConstant.CLUSTER_LIMITER;
import static org.hango.cloud.util.constant.PluginConstant.LOCAL_LIMITER;

/**
 * SmartLimiter处理器（转换本地限流和集群限流插件）
 * 详细设计及数据结构参考：https://kms.netease.com/article/65765
 *
 * @author yutao04
 * @since 2022.09.06
 */
@Component
public class SmartLimiterProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static final String STRATEGY_LOCAL = "single";
    private static final String STRATEGY_CLUSTER = "global";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MIN = "minute";
    private static final String SEC = "second";
    private static final List<String> TIME_UNIT_LIST = new ArrayList<>(Arrays.asList(DAY, HOUR, MIN, SEC));

    @Override
    public String getName() {
        return "SmartLimiterProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        String limitPluginType;

        if (source.contain("$.kind")) {
            limitPluginType = source.getValue("$.kind", String.class);
        } else {
            throw new RuntimeException("[smartLimiter plugin] 错误的插件结构，缺少kind根节点");
        }

        List<FragmentWrapper> smartLimiterActionList = new ArrayList<>();
        List<Map<String, Object>> limitList = source.getValue("$.limit_by_list", List.class);
        for (Map<String, Object> limitStrategy : limitList) {
            // 构建限流header匹配条件
            PluginGenerator headerMatch = generateHeaderMatch(limitPluginType, limitStrategy);

            // 构建限流action策略集合
            for (String timeUnit : TIME_UNIT_LIST) {
                PluginGenerator limitAction = generateLimitAction(limitStrategy, timeUnit, limitPluginType);
                // 插件配置不存在对应时间窗限流策略则不会生成对应的action
                if (limitAction == null) {
                    continue;
                }
                if (headerMatch != null) {
                    limitAction.createOrUpdateJson("$", "match", headerMatch.jsonString());
                }

                FragmentWrapper smartLimiterWrapper = new FragmentWrapper.Builder()
                        .withXUserId(getAndDeleteXUserId(source))
                        .withFragmentType(FragmentTypeEnum.SMART_LIMIT)
                        .withResourceType(K8sResourceEnum.SmartLimiter)
                        .withContent(limitAction.yamlString())
                        .build();
                smartLimiterActionList.add(smartLimiterWrapper);
            }
        }
        FragmentHolder fragmentHolder = new FragmentHolder();
        fragmentHolder.setSmartLimiterFragment(smartLimiterActionList);
        return fragmentHolder;
    }

    private PluginGenerator generateHeaderMatch(String limitPluginType, Map<String, Object> limitStrategy) {
        PluginGenerator headerMatch = null;
        List<Object> headerMatchList = (List) limitStrategy.get("headers");
        if (!CollectionUtils.isEmpty(headerMatchList)) {
            if (CLUSTER_GROUP_LIMITER.equals(limitPluginType)) {
                // 集群分组限流header匹配规则处理
                headerMatch = translateHeadersOfClusterLimiter(headerMatchList);
            } else if (LOCAL_LIMITER.equals(limitPluginType) || CLUSTER_LIMITER.equals(limitPluginType)) {
                // 本地和集群限流header匹配规则处理
                headerMatch = translateHeadersOfLimiter(headerMatchList);
            } else {
                throw new RuntimeException("[smartLimiter plugin] 错误的插件类型");
            }
        }
        return headerMatch;
    }

    /**
     * 构造基础的action结构，案例如下
     * - action:
     *     fill_interval:
     *       seconds: 10      # 限流时间窗10s
     *     quota: '2'         # 2个请求限流
     *     strategy: 'single' # 本地限流模式
     *   condition: 'true'    # 与NSF统一，网关不关心
     *
     * @param limitStrategy   插件中的限流策略
     * @param timeUnit        时间窗（当前支持日、时、分、秒，后续时间窗在方法内此进行扩展）
     * @param limitPluginType 插件类型（集群限流、本地限流）
     * @return action的CR内容
     */
    private PluginGenerator generateLimitAction(Map<String, Object> limitStrategy, String timeUnit, String limitPluginType) {
        if (!limitStrategy.containsKey(timeUnit)) {
            return null;
        }
        // 获取插件中的限流请求数
        long reqNumPerTimeUint = Long.parseLong(String.valueOf(limitStrategy.get(timeUnit)));
        if (reqNumPerTimeUint < 0L) {
            throw new RuntimeException("[smartLimiter plugin] 非法的请求限流数");
        }

        // 获取插件中的时间(SmartLimiter仅支持秒级别时间窗)
        long seconds;
        switch (timeUnit) {
            case DAY:
                seconds = TimeUnit.DAYS.toSeconds(1);
                break;
            case HOUR:
                seconds = TimeUnit.HOURS.toSeconds(1);
                break;
            case MIN:
                seconds = TimeUnit.MINUTES.toSeconds(1);
                break;
            case SEC:
                seconds = 1L;
                break;
            default:
                throw new RuntimeException("[smartLimiter plugin] 非法的时间周期");
        }

        // 判断是集群限流还是本地限流策略
        String strategy = "";
        switch (limitPluginType) {
            case CLUSTER_LIMITER:
            case CLUSTER_GROUP_LIMITER:
                strategy = STRATEGY_CLUSTER;
                break;
            case LOCAL_LIMITER:
                strategy = STRATEGY_LOCAL;
                break;
            default:
                break;
        }

        // 构造action CR结构
        PluginGenerator limitAction = PluginGenerator.newInstance("{}");
        PluginGenerator limitQuota = PluginGenerator.newInstance("{\"fill_interval\":{\"seconds\":" + seconds + "},\"quota\":\"" + reqNumPerTimeUint + "\", \"strategy\":\"" + strategy + "\"}");
        limitAction.createOrUpdateJson("$", "action", limitQuota.jsonString());
        limitAction.createOrUpdateJson("$", "condition", "true");

        return limitAction;
    }

    /**
     * 构建集群分组限流插件header组匹配规则
     *
     * @param headerMatchList 请求头匹配规则集合
     * @return 插件转换的CR内容
     */
    private PluginGenerator translateHeadersOfClusterLimiter(List<Object> headerMatchList) {
        PluginGenerator headerMatch = PluginGenerator.newInstance("[]");

        for (Object headerMatchObj : headerMatchList) {
            PluginGenerator headerMatchSource = PluginGenerator.newInstance(headerMatchObj, ResourceType.OBJECT);
            String headerKey = headerMatchSource.getValue("$.header_key", String.class);
            // key匹配模式
            headerMatch.addJsonElement("$", String.format(present_match_separate, headerKey));
        }

        if (CollectionUtils.isEmpty(headerMatchList)) {
            return null;
        } else {
            return headerMatch;
        }
    }

    /**
     * 构建本地限流和集群限流插件header组匹配规则
     *
     * @param headerMatchList 请求头匹配规则集合
     * @return 插件转换的CR内容
     */
    private PluginGenerator translateHeadersOfLimiter(List<Object> headerMatchList) {
        PluginGenerator headerMatch = PluginGenerator.newInstance("[]");

        for (Object headerMatchObj : headerMatchList) {
            PluginGenerator headerMatchSource = PluginGenerator.newInstance(headerMatchObj, ResourceType.OBJECT);
            String headerKey = headerMatchSource.getValue("$.headerKey", String.class);
            String matchType = headerMatchSource.getValue("match_type", String.class);
            String headerValue = headerMatchSource.getValue("value", String.class);
            boolean presentInvert = Boolean.parseBoolean(headerMatchSource.getValue("invert"));

            String matchTypeFormatter = translateMatchType(matchType, presentInvert);
            headerMatch.addJsonElement("$", String.format(matchTypeFormatter, headerKey, headerValue));
        }

        if (CollectionUtils.isEmpty(headerMatchList)) {
            return null;
        } else {
            return headerMatch;
        }
    }

    /**
     * 将插件schema中的匹配类型转换为SmartLimiter规定的类型
     *
     * @param schemaMatchType 匹配类型
     * @return 待格式化字符串
     */
    private String translateMatchType(String schemaMatchType, boolean isPresent) {
        String matchFormat;
        switch (schemaMatchType) {
            case "exact_match":
            case "=":
                matchFormat = exact_match;
                break;
            case "prefix_match":
                matchFormat = PREFIX_MATCH;
                break;
            case "!=":
                matchFormat = exact_invert_match;
                break;
            case "safe_regex_match":
            case "≈":
                matchFormat = regex_match;
                break;
            case "!≈":
                matchFormat = regex_invert_match;
                break;
            case "present":
                matchFormat = isPresent ? present_match : present_invert_match;
                break;
            default:
                throw new RuntimeException("[smartLimiter plugin] 非法匹配方式");
        }
        return matchFormat;
    }
}