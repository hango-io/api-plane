package org.hango.cloud.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sClient;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.processor.AggregateGatewayProcessor;
import org.hango.cloud.core.template.TemplateUtils;
import org.hango.cloud.meta.Plugin;
import org.hango.cloud.meta.PluginSupportConfig;
import org.hango.cloud.meta.PluginSupportDetail;
import org.hango.cloud.meta.dto.PluginOrderDTO;
import org.hango.cloud.meta.dto.PluginOrderItemDTO;
import org.hango.cloud.service.PluginService;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netmask.com
 * @date 2019/8/2
 **/
@Service
public class PluginServiceImpl implements PluginService {

    private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);

    private static final String PLUGIN_CONFIG = "plugin/%s/plugin-config.json";

    @Value("${pluginConfigEnv:route}")
    private String env;

    @Value("${globalPluginConfigEnv:route}")
    private String globalEnv;

    @Autowired
    private Configuration configuration;

    @Autowired
    private EditorContext editorContext;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    K8sClient k8sClient;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private AggregateGatewayProcessor gatewayProcessor;

    private static final String PLUGIN_MANAGER_TEMPLATE = "plugin/manager/plugin-manager-template.json";
    private static final String PLUGIN_SUPPORT_CONFIG = "plugin/manager/plugin-support-config.json";

    @Override
    public Plugin getPlugin(String name) {
        return getPlugin(env, name);
    }

    @Override
    public Map<String, Plugin> getPlugins() {
        return getPlugins(env);
    }

    @Override
    public String getSchema(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new ApiPlaneException(String.format("schema path:[%s] can not be null, please check your plugin-config.json", path));
        }
        return TemplateUtils.getTemplate(path, configuration).toString();
    }


    @Override
    public List<FragmentHolder> processPlugin(List<String> plugins, String pluginScope) {
        if (CollectionUtils.isEmpty(plugins)) {
            return Collections.emptyList();
        }
        return plugins.stream().filter(StringUtils::hasText).map(o -> gatewayProcessor.process(o, pluginScope)).collect(Collectors.toList());
    }


    @Override
    public List<PluginSupportDetail> getPluginSupportConfig(String gatewayKind) {
        Template support = TemplateUtils.getTemplate(PLUGIN_SUPPORT_CONFIG, configuration);
        List<PluginSupportConfig> supportList;
        try {
            supportList = objectMapper.readValue(String.valueOf(support), new TypeReference<List<PluginSupportConfig>>() {});
        } catch (JsonProcessingException e) {
            logger.error("error support content , {}", gatewayKind);
            return new ArrayList<>();

        }
        Optional<PluginSupportConfig> gatewaySupport = supportList.stream().filter(s -> s.getGatewayKind().equals(gatewayKind)).findFirst();
        if (!gatewaySupport.isPresent()) {
            logger.info("error gateway kind , {}", gatewayKind);
            return new ArrayList<>();
        }
        return gatewaySupport.get().getPlugins();
    }


    @Override
    public PluginOrderDTO getPluginOrderTemplate(String gatewayKind) {
        //查询pluginSupport配置
        List<PluginSupportDetail> pluginSupportDetails = getPluginSupportConfig(gatewayKind);
        if (CollectionUtils.isEmpty(pluginSupportDetails)){
            return null;
        }
        //查询pluginManager配置
        Template manager = TemplateUtils.getTemplate(PLUGIN_MANAGER_TEMPLATE, configuration);
        PluginOrderDTO pluginOrderDTO;
        try {
            pluginOrderDTO =  objectMapper.readValue(manager.toString(), PluginOrderDTO.class);
        } catch (JsonProcessingException e) {
            logger.error("parse plugin order template error, content:{}", manager, e);
            throw new ApiPlaneException("get plugin order template error");
        }
        //过滤插件
        List<String> supportPlugins = pluginSupportDetails.stream().map(PluginSupportDetail::getPlugin).collect(Collectors.toList());
        List<PluginOrderItemDTO> pluginOrderItemDTOS = pluginOrderDTO.getPlugins().stream().filter(p -> supportPlugins.contains(p.getName())).collect(Collectors.toList());
        pluginOrderDTO.setPlugins(pluginOrderItemDTOS);
        return pluginOrderDTO;
    }


    private Plugin getPlugin(String env, String name) {
        Plugin p = getPlugins(env).get(name);
        if (Objects.isNull(p)) throw new ApiPlaneException(String.format("plugin [%s] does not exit.", name));
        return p;
    }

    private Map<String, Plugin> getPlugins(String env) {
        Map<String, Plugin> ret = new LinkedHashMap<>();
        String pluginConfig = getPluginConfig(env);

        ResourceGenerator rg = ResourceGenerator.newInstance(pluginConfig);
        int itemCount = rg.getValue("$.item.length()");
        for (int i = 0; i < itemCount; i++) {
            ResourceGenerator p = ResourceGenerator.newInstance(rg.getValue(String.format("$.item[%s]", i)), ResourceType.OBJECT);
            Plugin plugin = p.object(Plugin.class);
            ret.put(plugin.getName(), plugin);
        }
        return ret;
    }

    private String getPluginConfig(String env) {
        if (StringUtils.hasText(env)) {
            return TemplateUtils.getTemplate(String.format(PLUGIN_CONFIG, env), configuration).toString();
        }
        throw new ApiPlaneException(String.format("the env:[%s] of plugin config can not be null.", env));
    }
}
