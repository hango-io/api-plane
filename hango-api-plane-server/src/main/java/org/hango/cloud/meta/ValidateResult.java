package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/9
 **/
public class ValidateResult {
    @JsonProperty("pass")
    private boolean pass = false;
    @JsonProperty("items")
    private List<ViolationItem> items = new ArrayList<>();

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public List<ViolationItem> getItems() {
        return items;
    }

    public void setItems(List<ViolationItem> items) {
        this.items = items;
    }
}
