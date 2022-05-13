package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hanjiahao
 * @date 2019/8/22
 **/
@Component
public class RateLimitProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "RateLimitProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        //todo: XUser
        FragmentHolder holder = new FragmentHolder();
        PluginGenerator total = PluginGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        String xUserId = getAndDeleteXUserId(total);

        List<Object> limits = total.getValue("$.limit_by_list");

        PluginGenerator rateLimitGen = PluginGenerator.newInstance("{\"rate_limits\":[]}");
        PluginGenerator shareConfigGen = PluginGenerator.newInstance("{\"domain\":\"qingzhou\",\"descriptors\":[]}");

        limits.forEach(limit -> {
            PluginGenerator rg = PluginGenerator.newInstance(limit, ResourceType.OBJECT, editorContext);
            // 频控计算的不同维度，例如second, minute, hour, day(month, year暂时不支持)
            getUnits(rg).forEach((unit, duration) -> {
                String descriptorId;
                if (rg.contain("$.limit_id")) {
                    descriptorId = rg.getValue("$.limit_id", String.class);
                } else {
                    // 根据每个limit的整个string + unit单位 + unit值做hash
                    descriptorId = "hash:" + Objects.hash(limit, unit, duration);
                }
                String headerDescriptor = getHeaderDescriptor(serviceInfo, xUserId, descriptorId);
                rateLimitGen.addJsonElement("$.rate_limits", createRateLimits(rg, serviceInfo, headerDescriptor, null));
                shareConfigGen.addJsonElement("$.descriptors", createShareConfig(rg, serviceInfo, headerDescriptor, unit, duration));
            });
        });
        holder.setSharedConfigFragment(
                new FragmentWrapper.Builder()
                        .withXUserId(xUserId)
                        .withFragmentType(FragmentTypeEnum.OTHERS)
                        .withResourceType(K8sResourceEnum.SharedConfig)
                        .withContent(shareConfigGen.yamlString())
                        .build()
        );
        holder.setVirtualServiceFragment(
                new FragmentWrapper.Builder()
                        .withXUserId(xUserId)
                        .withFragmentType(FragmentTypeEnum.VS_API)
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withContent(rateLimitGen.yamlString())
                        .build()
        );
        return holder;
    }

    private String createRateLimits(PluginGenerator rg, ServiceInfo serviceInfo, String headerDescriptor, String xUserId) {
        PluginGenerator vs = PluginGenerator.newInstance("{\"stage\":0,\"actions\":[]}");

        int length = 0;
        if (rg.contain("$.pre_condition")) {
            length = rg.getValue("$.pre_condition.length()");
        }
        // 如果condition数量为0，则使用generic_key，否则使用header_value_match
        if (length == 0) {
            vs.addJsonElement("$.actions",
                    String.format("{\"generic_key\":{\"descriptor_value\":\"%s\"}}", headerDescriptor));
        } else {
            vs.addJsonElement("$.actions",
                    String.format("{\"header_value_match\":{\"headers\":[],\"descriptor_value\":\"%s\"}}", headerDescriptor));
            String matchHeader = getMatchHeader(rg, "", "$.identifier_extractor");
            for (int i = 0; i < length; i++) {
                String operator = rg.getValue(String.format("$.pre_condition[%d].operator", i));
                String rightValue = rg.getValue(String.format("$.pre_condition[%d].right_value", i));
                boolean invertMatch = false;
                if (rg.contain(String.format("$.pre_condition[%d].custom_extractor", i))) {
                    matchHeader = getMatchHeader(rg, matchHeader, String.format("$.pre_condition[%d].custom_extractor", i));
                }
                if ("true".equalsIgnoreCase(rg.getValue(String.format("$.pre_condition[%d].invert", i), String.class))) {
                    invertMatch = true;
                }

                String expression;
                switch (operator) {
                    case "≈":
                        expression = escapeBackSlash(rightValue);
                        vs.addJsonElement("$.actions[0].header_value_match.headers",
                                String.format(header_safe_regex_invert, matchHeader, expression, invertMatch));
                        break;
                    case "!≈":
                        expression = escapeBackSlash(rightValue);
                        vs.addJsonElement("$.actions[0].header_value_match.headers",
                                String.format(header_safe_regex_invert, matchHeader, expression, !invertMatch));
                        break;
                    case "=":
                        expression = rightValue;
                        vs.addJsonElement("$.actions[0].header_value_match.headers",
                                String.format(header_exact_invert, matchHeader, expression, invertMatch));
                        break;
                    case "!=":
                        expression = rightValue;
                        vs.addJsonElement("$.actions[0].header_value_match.headers",
                                String.format(header_exact_invert, matchHeader, expression, !invertMatch));
                        break;
                    case "present":
                        vs.addJsonElement("$.actions[0].header_value_match.headers",
                                String.format("{\"name\":\"%s\",\"present_match\":true,\"invert_match\":%s}", matchHeader, invertMatch));
                        break;
                    default:
                        throw new ApiPlaneException(String.format("Unsupported $.config.limit_by_list.pre_condition.operator: %s", operator));
                }
            }
        }
        if (length == 0 && rg.contain("$.identifier_extractor") && !StringUtils.isEmpty(rg.getValue("$.identifier_extractor", String.class))) {
            String matchHeader = getMatchHeader(rg, "", "$.identifier_extractor");
            String descriptorKey = String.format("WithoutValueHeader[%s]", matchHeader);
            vs.addJsonElement("$.actions", String.format("{\"request_headers\":{\"header_name\":\"%s\",\"descriptor_key\":\"%s\"}}", matchHeader, descriptorKey));
        }
        return vs.jsonString();
    }

    private String createShareConfig(PluginGenerator rg, ServiceInfo serviceInfo, String headerDescriptor, String unit, Long duration) {
        PluginGenerator shareConfig;
        int length = 0;
        if (rg.contain("$.pre_condition")) {
            length = rg.getValue("$.pre_condition.length()");
        }
        if (length == 0 && rg.contain("$.identifier_extractor") && !StringUtils.isEmpty(rg.getValue("$.identifier_extractor", String.class))) {
            String matchHeader = getMatchHeader(rg, "", "$.identifier_extractor");
            String descriptorKey = String.format("WithoutValueHeader[%s]", matchHeader);
            shareConfig = PluginGenerator.newInstance(String.format("{\"key\":\"generic_key\",\"value\":\"%s\",\"descriptors\":[{\"key\":\"%s\",\"rate_limit\":{\"unit\":\"%s\",\"requests_per_unit\":%d}}]}",
                    headerDescriptor,
                    descriptorKey,
                    unit,
                    duration
            ));
        } else if (length == 0) {
            shareConfig = PluginGenerator.newInstance(String.format("{\"key\":\"generic_key\",\"value\":\"%s\",\"rate_limit\":{\"unit\":\"%s\",\"requests_per_unit\":%d}}",
                    headerDescriptor,
                    unit,
                    duration
            ));
        } else {
            shareConfig = PluginGenerator.newInstance(String.format("{\"key\":\"header_match\",\"value\":\"%s\",\"rate_limit\":{\"unit\":\"%s\",\"requests_per_unit\":%d}}",
                    headerDescriptor,
                    unit,
                    duration
            ));
        }
        return shareConfig.jsonString();
    }

    private String getMatchHeader(PluginGenerator rg, String defaultVal, String path) {
        if (!rg.contain(path)) return defaultVal;
        String extractor = rg.getValue(path, String.class);

        String matchHeader;
        Matcher matcher = Pattern.compile("Header\\[(.*)\\]").matcher(extractor);
        if (matcher.find()) {
            matchHeader = matcher.group(1);
        } else {
            throw new ApiPlaneException(String.format("Unsupported %s: %s", path, extractor));
        }
        return matchHeader;
    }

    private Map<String, Long> getUnits(PluginGenerator rg) {
        Map<String, Long> ret = new LinkedHashMap<>();
        String[][] map = new String[][]{
                {"$.second", "SECOND"},
                {"$.minute", "MINUTE"},
                {"$.hour", "HOUR"},
                {"$.day", "DAY"}
        };
        for (String[] obj : map) {
            Long duration = rg.getValue(obj[0], Long.class);
            if (rg.contain(obj[0]) && Objects.nonNull(duration)) {
                ret.put(obj[1], duration);
            }
        }
        return ret;
    }

    private String getHeaderDescriptor(ServiceInfo serviceInfo, String user, String id) {
        if (StringUtils.isBlank(user)) {
            user = "none";
        }
        return String.format("Service[%s]-User[%s]-Gateway[%s]-Api[%s]-Id[%s]", getServiceName(serviceInfo), user, getGateway(serviceInfo), getApiName(serviceInfo), id);
    }
}
