package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/12/17
 **/
@Component
public class TransformProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "TransformProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{\"request_transformations\":[{\"transformation_template\":{\"extractors\":{},\"headers\":{},\"query_param_operators\":{},\"path\":{},\"passthrough\":{},\"parse_body_behavior\":\"DontParse\"}}]}");
        if (source.contain("$.conditions")) {
            buildConditions(source, builder);
        }
        List<String> texts = new ArrayList<>();
        if (source.contain("$.headers")) {
            texts.addAll(source.getValue("$.headers[*].text"));
        }
        if (source.contain("$.querystrings")) {
            texts.addAll(source.getValue("$.querystrings[*].text"));
        }
        if (source.contain("$.url")) {
            texts.addAll(source.getValue("$.url[*].text"));
        }
        if (source.contain("$.path")) {
            texts.addAll(source.getValue("$.path[*].text"));
        }
        List<String> placeholders = new ArrayList<>();
        texts.stream().filter(Objects::nonNull).forEach(text -> {
            Matcher matcher = Pattern.compile("\\{\\{(.*?)\\}\\}").matcher(text);
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        });
        buildExtractors(placeholders, builder, serviceInfo);
        buildHeaders(source, builder);
        buildQueryParam(source, builder);
        buildUrl(source, builder);
        buildPath(source, builder);
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    private void buildConditions(ResourceGenerator source, ResourceGenerator builder) {
        builder.createOrUpdateJson("$.request_transformations[0]", "conditions", "[{\"headers\":[],\"query_parameters\":[]}]");
        if (source.contain("$.conditions.headers")) {
            int itemCount = source.getValue("$.conditions.headers.length()", Integer.class);
            for (int i = 0; i < itemCount; i++) {
                String op = source.getValue(String.format("$.conditions.headers[%s].op", i));
                String key = source.getValue(String.format("$.conditions.headers[%s].key", i));
                String text = source.getValue(String.format("$.conditions.headers[%s].text", i));
                builder.addJsonElement("$.request_transformations[0].conditions[0].headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", key, getRegexByOp(op, text)));
            }
        }
        if (source.contain("$.conditions.querystrings")) {
            int itemCount = source.getValue("$.conditions.querystrings.length()", Integer.class);
            for (int i = 0; i < itemCount; i++) {
                String op = source.getValue(String.format("$.conditions.querystrings[%s].op", i));
                String key = source.getValue(String.format("$.conditions.querystrings[%s].key", i));
                String text = source.getValue(String.format("$.conditions.querystrings[%s].text", i));
                builder.addJsonElement("$.request_transformations[0].conditions[0].query_parameters", String.format("{\"name\":\"%s\",\"value\":\"%s\",\"regex\":true}", key, getRegexByOp(op, text)));
            }
        }
    }

    private void buildExtractors(List<String> placeholders, ResourceGenerator builder, ServiceInfo serviceInfo) {
        for (String placeholder : placeholders) {
            if (placeholder.startsWith("url")) {
                Matcher matcher = Pattern.compile("url\\[(.*?)\\]").matcher(placeholder);
                if (matcher.find()) {
                    Integer index = Integer.parseInt(matcher.group(1)) + 1;
                    String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", serviceInfo.getUri(), index);
                    builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", replaceString(placeholder), value);
                }
            } else if (placeholder.startsWith("path")) {
                Matcher matcher = Pattern.compile("path\\[(.*?)\\]").matcher(placeholder);
                if (matcher.find()) {
                    Integer index = Integer.parseInt(matcher.group(1)) + 1;
                    String value = String.format("{\"path\":{},\"regex\":\"%s\",\"subgroup\":%s}", serviceInfo.getUri(), index);
                    builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", replaceString(placeholder), value);
                }
            } else if (placeholder.startsWith("querystrings")) {
                Matcher matcher = Pattern.compile("querystrings\\[(.*?)\\]").matcher(placeholder);
                if (matcher.find()) {
                    String queryParam = matcher.group(1);
                    String value = String.format("{\"queryParam\":\"%s\",\"regex\":\"(.*)\",\"subgroup\":1}", queryParam);
                    builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", replaceString(placeholder), value);
                }
            } else if (placeholder.startsWith("headers")) {
                Matcher matcher = Pattern.compile("headers\\[(.*?)\\]").matcher(placeholder);
                if (matcher.find()) {
                    String header = matcher.group(1);
                    String value = String.format("{\"header\":\"%s\",\"regex\":\"(.*)\",\"subgroup\":1}", header);
                    builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", replaceString(placeholder), value);
                }
            }
        }
    }

    private void buildHeaders(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("headers")) {
            Integer size = source.getValue("$.headers.size()");
            for (Integer i = 0; i < size; i++) {
                String key = source.getValue(String.format("$.headers[%s].key", i));
                String text = source.getValue(String.format("$.headers[%s].text", i));
                String action = source.getValue(String.format("$.headers[%s].action", i));

                String value;
                if (StringUtils.isEmpty(text)) {
                    value = String.format("{\"action\":\"%s\"}", action);
                } else {
                    value = String.format("{\"text\":\"%s\",\"action\":\"%s\"}", text, action);
                }
                builder.createOrUpdateJson("$.request_transformations[0].transformation_template.headers",
                                           replaceString(key), replaceString(value));
            }
        }
    }

    private void buildQueryParam(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("querystrings")) {
            Integer size = source.getValue("$.querystrings.size()");
            for (Integer i = 0; i < size; i++) {
                String key = source.getValue(String.format("$.querystrings[%s].key", i));
                String text = source.getValue(String.format("$.querystrings[%s].text", i));
                String action = source.getValue(String.format("$.querystrings[%s].action", i));

                String value;
                if (StringUtils.isEmpty(text)) {
                    value = String.format("{\"action\":\"%s\"}", action);
                } else {
                    value = String.format("{\"text\":\"%s\",\"action\":\"%s\"}", text, action);
                }
                builder.createOrUpdateJson("$.request_transformations[0].transformation_template"
                                           + ".query_param_operators", replaceString(key), replaceString(value));
            }
        }
    }

    private void buildUrl(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("url")) {
            Integer size = source.getValue("$.url.size()");
            for (Integer i = 0; i < size; i++) {
                String text = source.getValue(String.format("$.url[%s].text", i));
                if (haveNull(text)) continue;
                String value = String.format("{\"text\":\"%s\",\"action\":\"%s\"}", replaceString(text), "Action_Update");
                builder.createOrUpdateJson("$.request_transformations[0].transformation_template.headers", ":path", value);
            }
        }
    }

    private void buildPath(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("path")) {
            Integer size = source.getValue("$.path.size()");
            for (Integer i = 0; i < size; i++) {
                String text = source.getValue(String.format("$.path[%s].text", i));
                if (haveNull(text)) continue;
                String value = String.format("{\"text\":\"%s\"}", replaceString(text));
                builder.createOrUpdateJson("$.request_transformations[0].transformation_template", "path", value);
            }
        }
    }

    private  String replaceString(String str){
        return str.replaceAll("\\[|\\]", "_");
    }
}
