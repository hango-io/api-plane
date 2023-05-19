package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


public class RequestOperation {

    @JsonProperty(value = "Add")
    private Map<String, String> add;

    public Map<String, String> getAdd() {
        return add;
    }

    public void setAdd(Map<String, String> add) {
        this.add = add;
    }
}
