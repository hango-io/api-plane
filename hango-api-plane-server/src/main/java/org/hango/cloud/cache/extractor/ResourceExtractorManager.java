package org.hango.cloud.cache.extractor;


import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.HashMap;
import java.util.Map;

public class ResourceExtractorManager<T extends HasMetadata> {


    private Map<String,ResourceInfoExtractor> savedExtractor = new HashMap<>();



    public String getResourceInfo(T obj, String target, Object ... parameter){

        ResourceInfoExtractor selected = savedExtractor.get(target);
        if (selected == null){
            throw new IllegalArgumentException("no extractor found for target "+ target);
        }
        return selected.extractData((HasMetadata) obj,parameter);
    }

    public void addExtractor(String target, ResourceInfoExtractor extractor){
        savedExtractor.put(target,extractor);
    }



}
