package org.hango.cloud.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableList;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.hango.cloud.configuration.ext.MeshConfig;
import org.hango.cloud.util.freemarker.AutoRemoveDirective;
import org.hango.cloud.util.freemarker.IgnoreDirective;
import org.hango.cloud.util.freemarker.IndentationDirective;
import org.hango.cloud.util.freemarker.SupplyDirective;
import org.hango.cloud.util.interceptor.RestTemplateLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/18
 **/
@Configuration
@PropertySource(value = {"classpath:mesh-config.properties"})
public class ApiPlaneAutoBaseConfiguration {

    @Autowired
    private freemarker.template.Configuration freemarkerConfig;

    @Bean
    @Primary
    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        List<ClientHttpRequestInterceptor> interceptors = ImmutableList.of(new RestTemplateLogInterceptor());

        return restTemplateBuilder
                .interceptors(interceptors)
                .requestFactory(new InterceptingClientHttpRequestFactory(
                        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()), interceptors))
                .build();
    }

    @Bean
    @Qualifier("shortTimeoutRestTemplate")
    RestTemplate shortTimeoutRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        List<ClientHttpRequestInterceptor> interceptors = ImmutableList.of(new RestTemplateLogInterceptor());

        return restTemplateBuilder
                .setConnectTimeout(1000)
                .interceptors(interceptors)
                .requestFactory(new InterceptingClientHttpRequestFactory(
                        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()), interceptors))
                .build();
    }


    @Bean
    @Qualifier("yaml")
    YAMLMapper yamlObjectMapper() {
        YAMLMapper yamlMapper = new YAMLMapper();
        // 不输出---
        yamlMapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
        // 不输出引号
        yamlMapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return yamlMapper;
    }

    @Bean
    @Primary
    ObjectMapper jsonObjectMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new ProtobufModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @PostConstruct
    void configureFreemarkerConfig() {
        freemarkerConfig.setNumberFormat("#");
        freemarkerConfig.setSharedVariable("indent", new IndentationDirective());
        freemarkerConfig.setSharedVariable("ignore", new IgnoreDirective());
        freemarkerConfig.setSharedVariable("supply", new SupplyDirective());
        freemarkerConfig.setSharedVariable("autoremove", new AutoRemoveDirective());
    }

    @Bean
    MeshConfig meshConfig(){
        return new MeshConfig();
    }
}