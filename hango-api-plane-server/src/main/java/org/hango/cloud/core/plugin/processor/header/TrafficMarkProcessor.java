package org.hango.cloud.core.plugin.processor.header;

import com.google.gson.*;
import net.minidev.json.JSONArray;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.core.plugin.processor.AbstractSchemaProcessor;
import org.hango.cloud.meta.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 请求头重写插件处理器
 *
 * @author yutao04
 * @date 2022.3.22
 */
@SuppressWarnings({"java:S1192"})
@Component
public class TrafficMarkProcessor extends AbstractSchemaProcessor {
    private final Logger logger = LoggerFactory.getLogger(TrafficMarkProcessor.class);

    @Override
    public String getName() {
        return "TrafficMarkProcessor";
    }

    /**
     * @param plugin      前端到后台的插件参数
     * @param serviceInfo 服务信息
     * @return k8s的CRD片段
     * @link https://g.hz.netease.com/qingzhou/envoy-netease/-/blob/release-20210430/api/netease/filters/http/header_rewrite/v2/header_rewrite.proto
     * <p>
     * plugins:
     * - name: com.netease.filters.http.header_rewrite
     * settings:
     * config:
     * decoder_rewriters:
     * rewriters: #            多选
     * - header_name: # string 需要变更的header key
     * update: # string      需要更新的内容
     * append: # string      需要创建的内容
     * remove: {} # Empty    需要删除
     */
    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        logger.info("[traffic mark] start transform request header traffic mark");

        PluginGenerator pluginInfo = PluginGenerator.newInstance(plugin);

        if (!pluginInfo.contain("$.headerKey")) {
            logger.error("[traffic mark] illegal format without headerKey");
            return new FragmentHolder();
        }

        // 将前台数据转成可以构建yaml配置的builder对象
        PluginGenerator builder = transformDataToYamlBuilder(pluginInfo);

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder().withXUserId(getAndDeleteXUserId(pluginInfo)).withFragmentType(FragmentTypeEnum.VS_API).withResourceType(K8sResourceEnum.VirtualService).withContent(builder.yamlString()).build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    private PluginGenerator transformDataToYamlBuilder(PluginGenerator pluginInfo) {
        JsonArray headerJsonArray = new JsonParser().parse(pluginInfo.getValue("$.headerKey[*]", JSONArray.class).toJSONString()).getAsJsonArray();

        PluginGenerator builder = PluginGenerator.newInstance("{\"config\":{\"decoder_rewriters\":{\"rewriters\":[]}}}");

        for (JsonElement jsonElement : headerJsonArray) {
            // 获取数组元素对象
            JsonObject headerGroup = jsonElement.getAsJsonObject();
            JsonObject request = headerGroup.get("request").getAsJsonObject();


            PluginGenerator headerOperation = PluginGenerator.newInstance("{}");

            // 创建匹配条件
            getCondition(headerOperation, headerGroup);

            processRewriter(headerGroup, headerOperation);

            // 将一次对header的操作追加到builder的rewriters节点
            builder.addJsonElement("$.config.decoder_rewriters.rewriters", headerOperation.jsonString());
        }
        return builder;
    }

    private void processRewriter(JsonObject headerGroup, PluginGenerator headerOperation) {
        // 取单个对象的所有的值
        String headerKey = headerGroup.has("headerKey") ? headerGroup.get("headerKey").getAsString() : "";
        String headerValue = headerGroup.has("headerValue") ? headerGroup.get("headerValue").getAsString() : "";
        String operation = headerGroup.has("operation") ? headerGroup.get("operation").getAsString() : "";

        logger.info("[traffic mark] loop  operation: {}; headerKey: {}; headerValue: {}", operation, headerKey, headerValue);


        headerOperation.createOrUpdateJson("$", "header_name", headerKey);

        // 根据操作填充数据
        if (operation.equals(ResponseHeaderRewriteProcessor.HeaderOperation.CREATE.asString())) {
            headerOperation.createOrUpdateJson("$", "append", headerValue);
        } else if (operation.equals(ResponseHeaderRewriteProcessor.HeaderOperation.UPDATE.asString())) {
            headerOperation.createOrUpdateJson("$", "update", headerValue);
        } else if (operation.equals(ResponseHeaderRewriteProcessor.HeaderOperation.DELETE.asString())) {
            headerOperation.createOrUpdateJson("$", "remove", "{}");
        } else {
            throw new UnsupportedOperationException("operation can only be create|update|delete, input operation [" + operation + "] is illegal");
        }
    }

    private void getCondition(PluginGenerator headerOperation, JsonObject headerGroup) {
        JsonObject request = headerGroup.get("request").getAsJsonObject();
        if (!request.has("headers") && !request.has("parameters")) {
            return;
        }

        Gson gsonUtil = new Gson();
        headerOperation.createOrUpdateJson("$", "matcher", "{}");

        if (request.has("headers")) {
            headerOperation.createOrUpdateJson("$.matcher", "headers", "[]");
            List<Map<String, String>> headers = gsonUtil.fromJson(request.get("headers"), List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, headerKey, headerValue)) {
                    return;
                }
                switch (matchType) {
                    case "safe_regex_match":
                        headerOperation.addJsonElement("$.matcher.headers", String.format(safe_regex_string_match, headerKey, headerValue));
                        break;
                    case "exact_match":
                        headerOperation.addJsonElement("$.matcher.headers", String.format(exact_string_match, headerKey, headerValue));
                        break;
                    default:
                        headerOperation.addJsonElement("$.matcher.headers", String.format(prefix_string_match, headerKey, headerValue));
                        break;
                }
            });
        } else if (request.has("parameters")) {
            headerOperation.createOrUpdateJson("$.matcher", "parameters", "[]");
            List<Map<String, String>> parameters = gsonUtil.fromJson(request.get("parameters"), List.class);
            parameters.forEach(item -> {
                String matchType = item.get("match_type");
                String parameterKey = item.get("parameterKey");
                String headerValue = item.get("value");
                if (haveNull(matchType, parameterKey, headerValue)) {
                    return;
                }
                switch (matchType) {
                    case "safe_regex_match":
                        headerOperation.addJsonElement("$.matcher.parameters", String.format(safe_regex_string_match, parameterKey, headerValue));
                        break;
                    case "exact_match":
                        headerOperation.addJsonElement("$.matcher.parameters", String.format(exact_string_match, parameterKey, headerValue));
                        break;
                    default:
                        headerOperation.addJsonElement("$.matcher.parameters", String.format(prefix_string_match, parameterKey, headerValue));
                        break;
                }

            });
        }


    }
}