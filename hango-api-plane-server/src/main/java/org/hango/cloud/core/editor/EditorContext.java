package org.hango.cloud.core.editor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class EditorContext {
    private ObjectMapper jsonMapper;

    private ObjectMapper yamlMapper;

    private Configuration configuration;

    public ObjectMapper jsonMapper() {
        // NON_DEFAULT策略下的序列化规则： 值为"0\false\null"的字段将会被序列化忽略
        return jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    public ObjectMapper yamlMapper() {
        return yamlMapper;
    }

    public Configuration configuration() {
        return configuration;
    }

    public EditorContext(ObjectMapper jsonMapper, ObjectMapper yamlMapper, Configuration configuration) {
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.configuration = configuration;
    }
}
