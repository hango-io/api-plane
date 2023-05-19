package org.hango.cloud.core.plugin.processor.header;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.core.plugin.processor.AbstractSchemaProcessor;
import org.hango.cloud.meta.ServiceInfo;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResponseHeaderRewriteProcessor extends AbstractSchemaProcessor {
    private final Logger logger = LoggerFactory.getLogger(ResponseHeaderRewriteProcessor.class);

    @Override
    public String getName() {
        return "ResponseHeaderRewriteProcessor";
    }

    /**
     * @param plugin      前端到后台的插件参数
     * @param serviceInfo 服务信息
     * @return k8s的CRD片段
     * @link https://g.hz.netease.com/qingzhou/envoy-netease/-/blob/release-20210430/api/netease/filters/http/header_rewrite/v2/header_rewrite.proto
     *
     * plugins:
     *   - name: com.netease.filters.http.header_rewrite
     *     settings:
     *       config:
     *         encoder_rewriters:
     *           rewriters: #            多选
     *           - header_name: # string 需要变更的header key
     *             update: # string      需要更新的内容
     *             append: # string      需要创建的内容
     *             remove: {} # Empty    需要删除
     */
    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        logger.info("[rewrite plugin] start transform response header rewrite plugin");

        PluginGenerator pluginInfo = PluginGenerator.newInstance(plugin);

        if (!pluginInfo.contain("$.headerKey")) {
            logger.error("[rewrite plugin] illegal format without headerKey");
            return new FragmentHolder();
        }

        // 将前台数据转成可以构建yaml配置的builder对象
        PluginGenerator builder = transformDataToYamlBuilder(pluginInfo);

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(pluginInfo))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    private PluginGenerator transformDataToYamlBuilder(PluginGenerator pluginInfo) {
        JsonArray headerJsonArray = new JsonParser()
                .parse(pluginInfo.getValue("$.headerKey[*]", JSONArray.class).toJSONString())
                .getAsJsonArray();

        PluginGenerator builder = PluginGenerator.newInstance("{\"config\":{\"encoder_rewriters\":{\"rewriters\":[]}}}");

        for (int i = 0; i < headerJsonArray.size(); i++) {
            // 获取数组元素对象
            JsonObject headerGroup = headerJsonArray.get(i).getAsJsonObject();

            // 取单个对象的所有的值
            String headerKey = headerGroup.has("headerKey")
                    ? headerGroup.get("headerKey").getAsString()
                    : "";
            String headerValue = headerGroup.has("headerValue")
                    ? headerGroup.get("headerValue").getAsString()
                    : "";
            String operation = headerGroup.has("operation")
                    ? headerGroup.get("operation").getAsString()
                    : "";

            logger.info("[rewrite plugin] loop index {} --- operation: {}; headerKey: {}; headerValue: {}",
                    i, operation, headerKey, headerValue);

            PluginGenerator headerOperation = PluginGenerator.newInstance("{}");

            headerOperation.createOrUpdateJson("$", "header_name", headerKey);

            // 根据操作填充数据
            if (operation.equals(HeaderOperation.CREATE.asString())) {
                headerOperation.createOrUpdateJson("$", "append", headerValue);
            } else if (operation.equals(HeaderOperation.UPDATE.asString())) {
                headerOperation.createOrUpdateJson("$", "update", headerValue);
            } else if (operation.equals(HeaderOperation.DELETE.asString())) {
                headerOperation.createOrUpdateJson("$", "remove", "{}");
            } else {
                throw new UnsupportedOperationException(
                        "operation can only be create|update|delete, input operation [" + operation + "] is illegal");
            }

            // 将一次对header的操作追加到builder的rewriters节点
            builder.addJsonElement("$.config.encoder_rewriters.rewriters", headerOperation.jsonString());
        }

        return builder;
    }

    // 可选的头部操作
    enum HeaderOperation {
        CREATE("create"),
        UPDATE("update"),
        DELETE("delete");

        private final String strOperation;

        HeaderOperation(String strOperation) {
            this.strOperation = strOperation;
        }

        /**
         * 将枚举值转化为字符串
         *
         * @return 字符串类型的操作 (create|update|delete)
         */
        public String asString() {
            return strOperation;
        }
    }
}
