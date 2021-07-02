package org.hango.cloud.cache.extractor;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.Map;

public interface Extractor<T extends HasMetadata> {


    public String extractData(T obj ,Object... parameter);


    interface InternalExtractor {

        public KeyValueInfo extractByKey(Map<String,String> rawInfo, Object ... parameter);

        public String extractOnValue(String rawValue,Object ... parameter);
    }





}
