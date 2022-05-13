package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentTypeEnum;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class LuaProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "LuaProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper;
        PluginGenerator rg = PluginGenerator.newInstance("{\"resty\":{\"plugins\":[]}}");
        rg.addJsonElement("$.resty.plugins", plugin);
        String level = rg.getValue("$.resty.plugins[0].level");
        String xUserId = rg.getValue("$.resty.plugins[0].x_user_id");
        rg.removeElement("$.resty.plugins[0].level");
        rg.removeElement("$.resty.plugins[0].kind");
        rg.removeElement("$.resty.plugins[0].version");
        rg.removeElement("$.resty.plugins[0].x_user_id");
        switch (level) {
            case "host":
                wrapper = new FragmentWrapper.Builder()
                        .withContent(rg.yamlString())
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withFragmentType(FragmentTypeEnum.VS_HOST)
                        .withXUserId(xUserId)
                        .build();
                break;
            case "api":
                wrapper = new FragmentWrapper.Builder()
                        .withContent(rg.yamlString())
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withFragmentType(FragmentTypeEnum.VS_API)
                        .withXUserId(xUserId)
                        .build();
                break;
            case "match":
                wrapper = new FragmentWrapper.Builder()
                        .withContent(rg.yamlString())
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withFragmentType(FragmentTypeEnum.VS_MATCH)
                        .withXUserId(xUserId)
                        .build();
                break;
            default:
                throw new ApiPlaneException("Unsupported Lua plugin level:" + level);
        }
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> holders = plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());

        List<FragmentHolder> ret = new ArrayList<>();

        List<FragmentWrapper> hostLuas = new ArrayList<>();
        List<FragmentWrapper> apiLuas = new ArrayList<>();
        List<FragmentWrapper> matchLuas = new ArrayList<>();
        Object[][] wrapperMap = new Object[][]{
                new Object[]{hostLuas, FragmentTypeEnum.VS_HOST},
                new Object[]{apiLuas, FragmentTypeEnum.VS_API},
                new Object[]{matchLuas, FragmentTypeEnum.VS_MATCH}
        };
        // 根据 host api match级别分类lua插件
        for (Object[] item : wrapperMap) {
            item[0] = holders.stream()
                    .filter(holder -> holder.getVirtualServiceFragment().getFragmentType().equals(item[1]))
                    .map(FragmentHolder::getVirtualServiceFragment)
                    .collect(Collectors.toList());

            List<FragmentWrapper> luas = (List<FragmentWrapper>) item[0];
            if (CollectionUtils.isEmpty(luas)) continue;
            // 根据租户分类lua插件
            MultiValueMap<String, FragmentWrapper> userLuaMap = new LinkedMultiValueMap<>();
            luas.forEach(lua -> {
                String xUserId = lua.getXUserId();
                if (StringUtils.isEmpty(xUserId)) {
                    userLuaMap.add("NoneUser", lua);
                } else {
                    userLuaMap.add(xUserId, lua);
                }
            });

            for (Map.Entry<String, List<FragmentWrapper>> luaMap : userLuaMap.entrySet()) {
                PluginGenerator rg = PluginGenerator.newInstance("{\"resty\":{\"plugins\":[]}}");
                luaMap.getValue().forEach(lua -> rg.addElement("$.resty.plugins",
                        PluginGenerator.newInstance(lua.getContent(), ResourceType.YAML).getValue("$.resty.plugins[0]")));

                FragmentHolder holder = new FragmentHolder();
                FragmentWrapper wrapper = new FragmentWrapper.Builder()
                        .withContent(rg.yamlString())
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withFragmentType((FragmentTypeEnum) item[1])
                        .build();
                if (!"NoneUser".equals(luaMap.getKey())) {
                    wrapper.setXUserId(luaMap.getKey());
                }
                holder.setVirtualServiceFragment(wrapper);
                ret.add(holder);
            }

        }

        return ret;
    }
}
