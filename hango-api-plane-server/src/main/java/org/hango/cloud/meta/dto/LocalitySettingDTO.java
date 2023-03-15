package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/9/7 12:30
 **/
public class LocalitySettingDTO {

    @JsonProperty(value = "Enable")
    private Boolean enable;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
