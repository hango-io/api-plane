package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/9
 **/
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
