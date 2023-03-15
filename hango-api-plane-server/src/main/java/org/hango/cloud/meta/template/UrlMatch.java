package org.hango.cloud.meta.template;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



public class UrlMatch implements Serializable {

    private Map<String, String> urlMatch = new HashMap<>();

    @JsonAnySetter
    public void setUrlMatch(String type, String url) {
        this.urlMatch.put(type, url);
    }

    @JsonAnyGetter
    public Map<String, String> getUrlMatch() {
        return urlMatch;
    }
}
