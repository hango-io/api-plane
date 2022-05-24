package org.hango.cloud.meta.template;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;


public class Header {

    /**
     * header key值
     */
    private String header;

    private Map<String, String> headerMatch = new HashMap<>();

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setHeaderMatch(Map<String, String> headerMatch) {
        this.headerMatch = headerMatch;
    }

    @JsonAnySetter
    public void setHeaderMatch(String type, String header) {
        this.headerMatch.put(type, header);
    }

    @JsonAnyGetter
    public Map<String, String> getHeaderMatch() {
        return headerMatch;
    }
}
