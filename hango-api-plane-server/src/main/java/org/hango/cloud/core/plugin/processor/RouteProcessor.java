package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.gateway.service.ResourceManager;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.meta.Endpoint;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 路由插件的转换processor
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/7
 **/
@Component
public class RouteProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Autowired
    private ResourceManager resourceManager;

    @Override
    public String getName() {
        return "RouteProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{}");
        builder.createOrUpdateJson("$","direct_response", "{}");
        builder.createOrUpdateValue("$.direct_response", "status", source.getValue("$.code", Integer.class));
        builder.createOrUpdateJson("$.direct_response", "body", "{}");
        builder.createOrUpdateValue("$.direct_response.body", "inline_string", source.getValue("$.body", String.class));
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

    private String createPassProxy(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        List<Endpoint> endpoints = resourceManager.getEndpointList();

        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        customMatchAndPriority(rg, ret, info, xUserId);
        ret.createOrUpdateJson("$", "route", "[]");

        int length = rg.getValue("$.action.pass_proxy_target.length()");
        for (int i = 0; i < length; i++) {
            String targetHost = rg.getValue(String.format("$.action.pass_proxy_target[%d].url", i));
            Integer weight = rg.getValue(String.format("$.action.pass_proxy_target[%d].weight", i));
            Integer port = resourceManager.getServicePort(endpoints, targetHost);
            // 根据host查找host的port
            ret.addJsonElement("$.route",
                    String.format("{\"destination\":{\"host\":\"%s\",\"port\":{\"number\":%d},\"subset\":\"%s\"},\"weight\":%d}",
                            targetHost, port, info.getSubset(), weight));
        }
        return ret.jsonString();
    }

    private String createReturn(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        customMatchAndPriority(rg, ret, info, xUserId);
        ret.createOrUpdateJson("$", "return",
                String.format("{\"body\":{\"inlineString\":\"%s\"},\"code\":%s}", StringEscapeUtils.escapeJava(rg.getValue("$.action.return_target.body")), rg.getValue("$.action.return_target.code")));
        if (rg.contain("$.action.return_target.header")) {
            ret.createOrUpdateJson("$", "appendResponseHeaders", "{}");
            List<Object> headers = rg.getValue("$.action.return_target.header[*]");
            for (Object header : headers) {
                ResourceGenerator h = ResourceGenerator.newInstance(header, ResourceType.OBJECT);
                ret.createOrUpdateValue("$.appendResponseHeaders", h.getValue("$.name"), h.getValue("$.value"));
            }
        }
        return ret.jsonString();
    }

    private String createRedirect(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        customMatchAndPriority(rg, ret, info, xUserId);
        String target = rg.getValue("$.action.target", String.class);
        try {
            URI uri = new URI(target);
            String authority = uri.getAuthority();
            String path = uri.getPath();
            String query = uri.getQuery();
            String pathAndQuery = path;
            if (!StringUtils.isEmpty(query)) {
                pathAndQuery = String.format("%s?%s", path, query);
            }
            if (!StringUtils.isEmpty(authority)) {
                ret.createOrUpdateJson("$", "redirect", String.format("{\"uri\":\"%s\",\"authority\":\"%s\"}", pathAndQuery, authority));
            } else {
                ret.createOrUpdateJson("$", "redirect", String.format("{\"uri\":\"%s\"}", pathAndQuery));
            }
        } catch (URISyntaxException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
        return ret.jsonString();
    }

    private String createRewrite(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        customMatchAndPriority(rg, ret, info, xUserId);
        ret.createOrUpdateJson("$", "ext", "[]");
        ret.addJsonElement("$.ext", "{\"name\":\"com.netease.rewrite\",\"settings\":{\"request_transformations\":[{\"transformation_template\":{\"extractors\":{},\"headers\":{},\"parse_body_behavior\":\"DontParse\"}}]}}");
        String extractor = rg.getValue("$.action.rewrite_regex");
        String transformPath = rg.getValue("$.action.target", String.class);
        // 兼容旧的格式，例如rewrite_regex: /anything/{code} action.target:/anything/gg/{{code}}
        if (Pattern.compile("\\{(.*)\\}").matcher(extractor).find() && Pattern.compile("\\{\\{(.*)\\}\\}").matcher(transformPath).find()) {
            // 将/anything/{code}转换为/anything/(.*)
            Matcher extract = Pattern.compile("\\{(.*?)\\}").matcher(extractor);
            String path = extractor.replaceAll("\\{(.*?)\\}", "\\(\\.\\*\\)");
            int regexCount = 1;
            while (extract.find()) {
                String key = extract.group(1);
                String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", path, regexCount++);
                ret.createOrUpdateJson("$.ext[0].settings.request_transformations[0].transformation_template.extractors", key, value);
            }
            ret.createOrUpdateJson("$.ext[0].settings.request_transformations[0].transformation_template.headers", ":path", String.format("{\"text\":\"%s\"}", transformPath));
        } else {
            // 新的使用方式
            // 例如rewrite_regex: /anything/(.*)/(.*) $.action.target:/$2/$1
            Matcher matcher = Pattern.compile("\\$(\\d)").matcher(rg.getValue("$.action.target"));
            int regexCount = 0;
            while (matcher.find()) {
                int group = Integer.parseInt(matcher.group(1));
                if (group > regexCount) {
                    regexCount = group;
                }
            }
            String original = rg.getValue("$.action.rewrite_regex");
            String target = transformPath.replaceAll("(\\$\\d)", "{{$1}}");
            for (int i = 1; i <= regexCount; i++) {
                String key = "$" + i;
                String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", original, i);
                ret.createOrUpdateJson("$.ext[0].settings.request_transformations[0].transformation_template.extractors", key, value);
            }
            ret.createOrUpdateJson("$.ext[0].settings.request_transformations[0].transformation_template.headers", ":path", String.format("{\"text\":\"%s\"}", target));
        }
        return ret.jsonString();
    }

    public void customMatchAndPriority(ResourceGenerator rg, ResourceGenerator ret, ServiceInfo info, String xUserId) {
        // priority = 默认路由priority+1
        ret.createOrUpdateJson("$", "priority", info.getPriority());
        // 如果插件没有自带的match条件，则不渲染match
        // 后续freemarker <supply></supply>标记会自动填充match
        if (!rg.contain("$.matcher") || rg.getValue("$.matcher.length()", Integer.class) == 0) {
            return;
        }
        ret.createOrUpdateJson("$", "match", createMatch(rg, info, xUserId));
    }
}
