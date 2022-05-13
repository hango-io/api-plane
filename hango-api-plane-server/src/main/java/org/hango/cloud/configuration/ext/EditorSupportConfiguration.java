package org.hango.cloud.configuration.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.util.EnumSet;
import java.util.Set;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
@org.springframework.context.annotation.Configuration
public class EditorSupportConfiguration {
    @Bean
    public Configuration configuration(ObjectMapper objectMapper) {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider(objectMapper);
            private final MappingProvider mappingProvider = new JacksonMappingProvider(objectMapper);

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
        return Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS).build();
    }

    @Bean
    public EditorContext editorContext(ObjectMapper jsonMapper, @Qualifier("yaml") YAMLMapper yamlMapper, Configuration configuration) {
        EditorContext editorContext = new EditorContext(jsonMapper, yamlMapper, configuration);

        ResourceGenerator.configDefaultContext(new EditorContext(jsonMapper, yamlMapper, configuration));
        // 特殊Generator 输出引号
        PluginGenerator.configDefaultContext(new EditorContext(jsonMapper, yamlMapper.copy().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, false), configuration));
        return editorContext;
    }
}
