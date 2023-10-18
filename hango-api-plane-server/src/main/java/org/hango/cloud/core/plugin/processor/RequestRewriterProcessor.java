package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.HttpOperate;
import org.hango.cloud.meta.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/8/17
 */
@Component
public class RequestRewriterProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo>{
    private static final Logger logger = LoggerFactory.getLogger(RequestRewriterProcessor.class);

    @Override
    public String getName() {
        return "RequestRewriterProcessor";
    }

    public static final String APPEND = "append";
    public static final String UPDATE = "update";
    public static final String REMOVE = "remove";

    /**
     * schema data:
     * {\n" +
     *             "  \"headers\": [\n" +
     *             "    {\n" +
     *             "      \"key\": \"x-test-header\",\n" +
     *             "      \"action\": \"update\",\n" +
     *             "      \"text\": \"header_value\"\n" +
     *             "    }\n" +
     *             "  ],\n" +
     *             "  \"kind\": \"transformer\",\n" +
     *             "  \"querystrings\": [\n" +
     *             "    {\n" +
     *             "      \"key\": \"queryKey\",\n" +
     *             "      \"action\": \"remove\"\n" +
     *             "    }\n" +
     *             "  ],\n" +
     *             "
     *  "}
     *
     * plugin data:
     * decoder_rewriters:
     *   rewriters:
     *   - update: "header_value"
     *     header_name: "x-test-header"
     *   - remove: {}
     *     parameter: "queryKey"
     */

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{\"config\":{\"decoder_rewriters\":{\"rewriters\":[]}} }");
        List<HttpOperate> operates = new ArrayList<>();
        if (source.contain("$.headers")) {
            operates.addAll(getCondition(source, "headers"));
        }
        if (source.contain("$.querystrings")) {
            operates.addAll(getCondition(source, "querystrings"));
        }
        operateValid(operates, plugin);
        buildRewrite(builder, operates);
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.ENVOY_PLUGIN)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setGatewayPluginsFragment(wrapper);
        return fragmentHolder;
    }

    private void buildRewrite(ResourceGenerator builder, List<HttpOperate> operates) {
        for (HttpOperate operate : operates) {
            PluginGenerator item = PluginGenerator.newInstance("{}");
            item.createOrUpdateJson("$", operate.getAction(), operate.getValue());
            item.createOrUpdateJson("$", operate.getType(), operate.getKey());
            builder.addJsonElement("$.config.decoder_rewriters.rewriters", item.jsonString());
        }
    }

    private void operateValid(List<HttpOperate> operates, String plugin){
        if (CollectionUtils.isEmpty(operates)){
            logger.error("operate is empty, config:{}", plugin);
            throw new IllegalArgumentException("operate is empty");
        }
        for (HttpOperate operate : operates) {
            if (!StringUtils.hasText(operate.getAction()) || !StringUtils.hasText(operate.getKey()) || !StringUtils.hasText(operate.getType())){
                logger.error("operate is invalid, config:{}", plugin);
                throw new IllegalArgumentException("operate is invalid");
            }
        }
    }

    private List<HttpOperate> getCondition(ResourceGenerator source, String type) {
        List<HttpOperate> operates = new ArrayList<>();
        Integer size = source.getValue(String.format("$.%s.size()", type));
        String index = String.format("$.%s", type);
        for (Integer i = 0; i < size; i++) {
            String key = source.getValue(String.format("%s[%s].key", index, i));
            String action = source.getValue(String.format("%s[%s].action", index, i));
            String text = Arrays.asList(APPEND, UPDATE).contains(action) ? source.getValue(String.format("%s[%s].text", index, i)) : "{}";
            HttpOperate operate = HttpOperate.of(getOperateType(type), action, key, text);
            operates.add(operate);
        }
        return operates;
    }

    private String getOperateType(String type){
        if ("headers".equals(type)){
            return "header_name";
        }
        if ("querystrings".equals(type)) {
            return "parameter";
        }
        return type;
    }
}
