package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class GlobalPluginsDeleteDTO {

    @JsonProperty(value = "Plugins")
    @NotEmpty(message = "Plugins")
    private List<String> plugins;

    @JsonProperty(value = "Code")
    @NotNull(message = "Code")
    private String code;

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
