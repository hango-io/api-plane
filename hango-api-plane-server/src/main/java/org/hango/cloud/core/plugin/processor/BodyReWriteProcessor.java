package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"java:S1192"})
public abstract class BodyReWriteProcessor extends AbstractSchemaProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public final FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        logger.info("[{}] process in plugin:{}", this.getClass().getCanonicalName(), plugin);
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        //处理匹配条件
        PluginGenerator matcher = null;
        if (hasRequestMatcher(source) || hasResponseMatcher(source)) {
            matcher = PluginGenerator.newInstance("{}");
            if (hasRequestMatcher(source)) {
                //1.创建请求匹配条件
                matcher.createOrUpdateJson("$", "decoder_matcher", "{\"headers\":[]}");
                //2.处理请求host匹配
                processHostMatcher(source, matcher);
                //3.处理请求方法匹配
                processMethodMatcher(source, matcher);
                //4.处理请求路径匹配
                processPathMatcher(source, matcher);
                //5.处理请求头匹配
                processRequestHeaderMatcher(source, matcher);
            }
            if (hasResponseMatcher(source)) {
                //6.创建响应匹配条件
                matcher.createOrUpdateJson("$", "encoder_matcher", "{\"headers\":[]}");
                //7.处理响应头匹配
                processResponseHeaderMatcher(source, matcher);
                //8.处理状态码匹配
                processResponseCodeMatcher(source, matcher);
            }
            logger.info("[{}] process matcher:{}", this.getClass().getCanonicalName(), matcher.jsonString());
        }
        //处理body转换配置
        PluginGenerator transformation = PluginGenerator.newInstance("{}");
        transformation.createOrUpdateJson("$", "json_transformations", "[]");
        processJsonTransformations(source, transformation);
        logger.info("[{}] process transformation:{}", this.getClass().getCanonicalName(), transformation.jsonString());

        //根据matcher和transformation生产插件配置
        PluginGenerator pluginConfig = generatePluginConfig(matcher, transformation);

        logger.info("[{}] process pluginConfig:{}", this.getClass().getCanonicalName(), pluginConfig.jsonString());
        logger.info("[{}] process pluginConfig.yamlString:{}", this.getClass().getCanonicalName(), pluginConfig.yamlString());

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(pluginConfig.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    private void processPathMatcher(PluginGenerator source, PluginGenerator matcher) {
        if (source.contain("$.request.path")) {
            String matchType = source.getValue("$.request.path.match_type", String.class);
            String path = source.getValue("$.request.path.value", String.class);
            if (nonNull(matchType, path)) {
                if ("safe_regex_match".equals(matchType)) {
                    matcher.addJsonElement("$.decoder_matcher.headers", String.format(safe_regex_string_match, ":path", path));
                } else {
                    matcher.addJsonElement("$.decoder_matcher.headers", String.format(exact_string_match, ":path", path));
                }
            }
        }
    }

    private void processHostMatcher(PluginGenerator source, PluginGenerator matcher) {
        String matchType = source.getValue("$.request.host.match_type", String.class);
        String host = source.getValue("$.request.host.value", String.class);
        if (nonNull(matchType, host)) {
            if ("safe_regex_match".equals(matchType)) {
                matcher.addJsonElement("$.decoder_matcher.headers", String.format(safe_regex_string_match, ":authority", host));
            } else {
                matcher.addJsonElement("$.decoder_matcher.headers", String.format(exact_string_match, ":authority", host));
            }
        }
    }

    private void processMethodMatcher(PluginGenerator source, PluginGenerator matcher) {
        if (source.contain("$.request.method")) {
            @SuppressWarnings("unchecked")
            List<String> method = source.getValue("$.request.method", List.class);
            if (nonNull(method)) {
                if (method.size() == 1) {
                    matcher.addJsonElement("$.decoder_matcher.headers", String.format(exact_string_match, ":method", method.get(0)));
                } else if (method.size() > 1) {
                    matcher.addJsonElement("$.decoder_matcher.headers", String.format(safe_regex_string_match, ":method", String.join("|", method)));
                }
            }
        }
    }

    private void processRequestHeaderMatcher(PluginGenerator source, PluginGenerator matcher) {
        if (source.contain("$.request.headers")) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> headers = source.getValue("$.request.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, headerKey)) {
                    return;
                }
                switch (matchType) {
                    case "safe_regex_match":
                        matcher.addJsonElement("$.decoder_matcher.headers", String.format(safe_regex_string_match, headerKey, headerValue));
                        break;
                    case "present_match":
                        matcher.addJsonElement("$.decoder_matcher.headers", String.format(present_match, headerKey));
                        break;
                    case "present_match_invert":
                        matcher.addJsonElement("$.decoder_matcher.headers", String.format(present_invert_match, headerKey));
                        break;
                    default:
                        matcher.addJsonElement("$.decoder_matcher.headers", String.format(exact_string_match, headerKey, headerValue));
                        break;
                }
            });
        }
    }

    private void processResponseHeaderMatcher(PluginGenerator source, PluginGenerator matcher) {
        if (source.contain("$.response.headers")) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> headers = source.getValue("$.response.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, headerKey)) {
                    return;
                }
                if ("safe_regex_match".equals(matchType)) {
                    matcher.addJsonElement("$.encoder_matcher.headers", String.format(safe_regex_string_match, headerKey, headerValue));
                } else if ("present_match".equals(matchType)) {
                    matcher.addJsonElement("$.encoder_matcher.headers", String.format(present_match, headerKey));
                } else if ("present_match_invert".equals(matchType)) {
                    matcher.addJsonElement("$.encoder_matcher.headers", String.format(present_invert_match, headerKey));
                } else {
                    matcher.addJsonElement("$.encoder_matcher.headers", String.format(exact_string_match, headerKey, headerValue));
                }
            });
        }
    }

    private void processResponseCodeMatcher(PluginGenerator source, PluginGenerator matcher) {
        if (source.contain("$.response.code")) {
            String code = source.getValue("$.response.code.value", String.class);
            if (nonNull(code)) {
                matcher.addJsonElement("$.encoder_matcher.headers", String.format(safe_regex_string_match, ":status", code+"|"));
            }
        }
    }

    private static void processJsonTransformations(PluginGenerator source, PluginGenerator transformation) {
        //ArrayMinLength(1)控制最少一条body转换配置，此处bodyTransformList一定非空
        @SuppressWarnings("unchecked")
        List<Map<String, String>> headers =  source.getValue("$.bodyTransformList", List.class);
        headers.forEach(item -> {
            String jsonPointer = item.get("key");
            String jsonValue = item.get("value");
            //value均为字符类型不写json，使用字符串拼接处理转义问题
            String transformationElement = "{\"json_pointer\":\"" + jsonPointer + "\",\"json_value\":\"" + jsonValue + "\"}";
            transformation.addJsonElement("$.json_transformations", transformationElement);
        });
    }

    private boolean hasRequestMatcher(PluginGenerator source) {
        return source.contain("$.request.requestSwitch") && source.getValue("$.request.requestSwitch", Boolean.class);
    }

    private boolean hasResponseMatcher(PluginGenerator source) {
        return needMatchResponse() && source.contain("$.response.responseSwitch") && source.getValue("$.response.responseSwitch", Boolean.class);
    }

    /**
     * 是否需要响应匹配
     *
     * @return
     */
    protected abstract boolean needMatchResponse();

    protected abstract String getConfigKey();

    private PluginGenerator generatePluginConfig(PluginGenerator matcher, PluginGenerator transformation) {
        String configKey = getConfigKey();
        PluginGenerator pluginGenerator = PluginGenerator.newInstance("{\"" + configKey + "\":{}}");
        if (matcher != null) {
            pluginGenerator.createOrUpdateJson("$." + configKey, "matcher", matcher.jsonString());
        }
        pluginGenerator.createOrUpdateJson("$." + configKey, "json_body_transformation", transformation.jsonString());
        return pluginGenerator;
    }
}
