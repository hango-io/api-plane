package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hango.cloud.meta.ViolationItem;

import java.util.ArrayList;
import java.util.List;


public class ValidateResultDTO {

    @JsonProperty("Pass")
    private boolean pass = false;

    @JsonProperty("Items")
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
