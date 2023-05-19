package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

public class PairMatchDTO {

    @NotEmpty(message = "string match key")
    @JsonProperty(value = "Key")
    private String key;

    @NotEmpty(message = "string match value")
    @JsonProperty(value = "Value")
    private String value;

    @JsonProperty(value = "Type")
    @Pattern(regexp = "(exact|regex|prefix)", message = "string match type")
    private String type;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
