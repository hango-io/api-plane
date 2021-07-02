package org.hango.cloud.core.editor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;

public class EditorContext {
    private ObjectMapper jsonMapper;

    private ObjectMapper yamlMapper;

    private Configuration configuration;

    public ObjectMapper jsonMapper() {
        return jsonMapper;
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
