package org.hango.cloud.cache.extractor;


import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceInfoExtractor<T extends HasMetadata> implements Extractor{


    public static final String POSITION_LABEL = "label";
    public static final String POSITION_ANNOTATION = "Annotation";
    public static final String POSITION_SELECTOR = "selector";
    public static final String EXTRACTOR_TYPE_REGEX = "regex";
    public static final String EXTRACTOR_TYPE_EXACT = "exact";
    public static final String EXTRACTOR_TYPE_PATTERN = "pattern";
    public static final String RETURN_FROM_KEY = "key";


    private ResourceExtractorConfigProperties.ExtractorConfig config;

    private InternalExtractor keyExtractor;

    private InternalExtractor valueExtractor;

    public ResourceExtractorConfigProperties.ExtractorConfig getConfig() {
        return config;
    }

    public void setConfig(ResourceExtractorConfigProperties.ExtractorConfig config) {
        this.config = config;
    }

    public InternalExtractor getKeyExtractor() {
        return keyExtractor;
    }

    public void setKeyExtractor(InternalExtractor keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    public InternalExtractor getValueExtractor() {
        return valueExtractor;
    }

    public void setValueExtractor(InternalExtractor valueExtractor) {
        this.valueExtractor = valueExtractor;
    }

    public static ResourceInfoExtractor createExtractor(ResourceExtractorConfigProperties.ExtractorConfig config){
        ResourceInfoExtractor extractor = new ResourceInfoExtractor();
        extractor.setConfig(config);
        extractor.setKeyExtractor(createInternalExtractor(config.getKeyConfig()));
        extractor.setValueExtractor(createInternalExtractor(config.getValueConfig()));
        return extractor;
    }

    static InternalExtractor createInternalExtractor(ResourceExtractorConfigProperties.InternalExtractorConfig config){
        if (config == null){
            return null;
        }
        if (config.getType().equals(EXTRACTOR_TYPE_REGEX)){
            return new RegexInterExtractor(config.getPattern());
        }else if (config.getType().equals(EXTRACTOR_TYPE_EXACT)){
            return new ExactInterExtractor(config.getPattern());
        }else if (config.getType().equals(EXTRACTOR_TYPE_PATTERN)){
            return new PatternInterExtractor(config.getPattern(),config.getDefaultParameter());
        }else {
            throw new IllegalArgumentException("unsupported InternalExtractor type");
        }
    }

    @Override
    public String extractData(HasMetadata obj, Object... parameter) {

        if (config.getPosition().equals(POSITION_LABEL)){
            return doExtract(obj.getMetadata().getLabels(),parameter);
        }else if (config.getPosition().equals(POSITION_ANNOTATION)) {
            return doExtract(obj.getMetadata().getAnnotations(),parameter);
        }else if (config.getPosition().equals(POSITION_SELECTOR)){
            Service svc = (Service)obj;
            return doExtract(svc.getSpec().getSelector(),parameter);
        }else {
            throw new RuntimeException("unsupported location on resource definition");
        }
    }

    public String doExtract(Map<String, String> map, Object... parameter){

        KeyValueInfo kvInfo = keyExtractor.extractByKey(map,parameter);
        if (kvInfo == null){
            return null;
        }
        if (valueExtractor == null){
            if (config.getReturnFrom().equals(RETURN_FROM_KEY)){
                return kvInfo.getKey();
            }else{
                return kvInfo.getValue();
            }
        }else {
            if (config.getReturnFrom().equals(RETURN_FROM_KEY)){
                return valueExtractor.extractOnValue(kvInfo.getKey(),parameter);
            }else{
                return valueExtractor.extractOnValue(kvInfo.getValue(),parameter);
            }
        }
    }


    static class ExactInterExtractor implements InternalExtractor{

        private String pattern;

        public ExactInterExtractor(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public KeyValueInfo extractByKey(Map<String, String> rawInfo,Object ... parameter) {
            String value = rawInfo.get(pattern);
            return (value == null)?null:new KeyValueInfo(pattern,value);
        }

        @Override
        public String extractOnValue(String rawValue,Object ... parameter) {
            return rawValue;

        }
    }


    static class RegexInterExtractor implements InternalExtractor {

        private Pattern pattern;

        public RegexInterExtractor(String pattern) {
            this.pattern =  Pattern.compile(pattern);
        }

        @Override
        public KeyValueInfo extractByKey(Map<String, String> rawInfo,Object ... parameter) {
            if (rawInfo == null || rawInfo.isEmpty()){
                return null;
            }
            for (Map.Entry<String,String> entry : rawInfo.entrySet()){
                Matcher matcher = pattern.matcher((CharSequence) entry.getKey());
                if (matcher.find()){
                    return new KeyValueInfo(entry.getKey(),entry.getValue());
                }
            }
            return null;
        }

        @Override
        public String extractOnValue(String rawValue,Object ... parameter) {
            Matcher matcher = pattern.matcher(rawValue);
            if (matcher.find()){
                return matcher.group(0);
            }else {
                return null;
            }
        }


    }

    static class PatternInterExtractor implements InternalExtractor {

        private String pattern;

        private String defaultParameter;

        public PatternInterExtractor(String pattern,String defaultParameter) {
            this.pattern = pattern;
            this.defaultParameter = defaultParameter;
        }

        private static final String PLACEHOLDER_PREFIX = "${";
        private static final String PLACEHOLDER_SUFFIX = "}";

        @Override
        public KeyValueInfo extractByKey(Map<String, String> rawInfo,Object ... parameter) {
            String key = pattern;
            if (parameter.length < 1 && !StringUtils.isEmpty(defaultParameter)){
                String placeHolder = PLACEHOLDER_PREFIX + 0 + PLACEHOLDER_SUFFIX;
                key = key.replace(placeHolder,defaultParameter);
            }else {
                for (int i=0;i<parameter.length;i++){
                    String placeHolder = PLACEHOLDER_PREFIX + i + PLACEHOLDER_SUFFIX;
                    key = key.replace(placeHolder,String.valueOf(parameter[i]));
                }
            }
            String value = rawInfo.get(key);
            return (value == null)?null:new KeyValueInfo(key,value);
        }

        @Override
        public String extractOnValue(String rawValue,Object ... parameter) {
            return pattern;
        }
    }
}
