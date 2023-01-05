package org.hango.cloud.meta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.meta.dto.DubboInfoDto;
import org.hango.cloud.meta.dto.DubboMetaDto;

import java.util.List;
import java.util.Map;


/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 自定义资源meta枚举
 * @date 2022/8/16
 */
public enum CRDMetaEnum {

    VIRTUAL_SERVICE_STATS_META(VirtualService.class, "StatsMeta", TemplateConst.VIRTUAL_SERVICE_STATS, new TypeReference<Map<String,String>>() {
    }),
    VIRTUAL_SERVICE_DUBBO_META(VirtualService.class, "DubboMeta", TemplateConst.VIRTUAL_SERVICE_DUBBO, new TypeReference<DubboInfoDto>() {
    }),
    ;


    private Class<? extends KubernetesResource> target;

    private String name;

    private String templateName;

    private TypeReference transModel;

    CRDMetaEnum(Class<? extends KubernetesResource> target, String name, String templateName, TypeReference transModel) {
        this.target = target;
        this.name = name;
        this.templateName = templateName;
        this.transModel = transModel;
    }

    public TypeReference getTransModel() {
        return transModel;
    }

    public void setTransModel(TypeReference transModel) {
        this.transModel = transModel;
    }

    public Class<? extends KubernetesResource> getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }

    public String getTemplateName() {
        return templateName;
    }

    /**
     * 通过名称获取模板名称
     *
     * @return
     */
    public static CRDMetaEnum get(Class<? extends KubernetesResource> target, String name) {
        for (CRDMetaEnum value : values()) {
            if (value.target.equals(target) && value.name.equals(name)) {
                return value;
            }
        }
        return null;

    }

    /**
     * 获取上层传输的Meta信息内容
     *
     * @param content
     * @param <T>
     * @return
     * @throws JsonProcessingException
     */
    public <T> T getTransData(String content) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return ((T) objectMapper.readValue(content, this.transModel));

    }

}
