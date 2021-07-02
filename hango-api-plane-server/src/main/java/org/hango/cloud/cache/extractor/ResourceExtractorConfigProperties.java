package org.hango.cloud.cache.extractor;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@ConfigurationProperties("extractor")
@Component
public class ResourceExtractorConfigProperties {


    private List<ExtractorConfig> configList;

    public List<ExtractorConfig> getConfigList() {
        return configList;
    }

    public void setConfigList(List<ExtractorConfig> configList) {
        this.configList = configList;
    }


    public static class ExtractorConfig {

        private InternalExtractorConfig keyConfig;
        private InternalExtractorConfig valueConfig;
        private String returnFrom;
        private String target;

        public ExtractorConfig() {
        }

        public InternalExtractorConfig getKeyConfig() {
            return keyConfig;
        }

        public void setKeyConfig(InternalExtractorConfig keyConfig) {
            this.keyConfig = keyConfig;
        }

        public InternalExtractorConfig getValueConfig() {
            return valueConfig;
        }

        public void setValueConfig(InternalExtractorConfig valueConfig) {
            this.valueConfig = valueConfig;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getReturnFrom() {
            return returnFrom;
        }

        public void setReturnFrom(String returnFrom) {
            this.returnFrom = returnFrom;
        }

        private String position;

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

    }

    public static class InternalExtractorConfig {

        public String type;
        public String pattern;
        public String defaultParameter;

        public String getDefaultParameter() {
            return defaultParameter;
        }

        public void setDefaultParameter(String defaultParameter) {
            this.defaultParameter = defaultParameter;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

    @Bean
    ResourceExtractorManager resourceExtractorManager(ResourceExtractorConfigProperties properties){

        ResourceExtractorManager manager = new ResourceExtractorManager();
        if (CollectionUtils.isEmpty(properties.getConfigList())){
            return manager;
        }
        for (ExtractorConfig extractorConfig : properties.getConfigList()) {
            ResourceInfoExtractor extractor = ResourceInfoExtractor.createExtractor(extractorConfig);
            manager.addExtractor(extractorConfig.getTarget(),extractor);
        }
        return manager;
    }


}
