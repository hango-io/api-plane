package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * ratelimit configmap中的模型
 */
public class ConfigMapRateLimit {

    @JsonProperty("descriptors")
    private List<ConfigMapRateLimitDescriptor> descriptors = new ArrayList();

    @JsonProperty("domain")
    private String domain;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConfigMapRateLimitDescriptor {

        @JsonProperty("key")
        private String key;

        @JsonProperty("rate_limit")
        private ConfigMapRateLimitInner rateLimit;

        @JsonProperty("value")
        private String value;

        @JsonProperty("descriptors")
        private List<ConfigMapRateLimitDescriptor> descriptors;

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

        public ConfigMapRateLimitInner getRateLimit() {
            return rateLimit;
        }

        public void setRateLimit(ConfigMapRateLimitInner rateLimit) {
            this.rateLimit = rateLimit;
        }

        public List<ConfigMapRateLimitDescriptor> getDescriptors() {
            return descriptors;
        }

        public void setDescriptors(List<ConfigMapRateLimitDescriptor> descriptors) {
            this.descriptors = descriptors;
        }
    }

    public static class ConfigMapRateLimitInner {
        @JsonProperty("requests_per_unit")
        private Integer requestsPerUnit;

        @JsonProperty("unit")
        private Object unit;

        public Integer getRequestsPerUnit() {
            return requestsPerUnit;
        }

        public void setRequestsPerUnit(Integer requestsPerUnit) {
            this.requestsPerUnit = requestsPerUnit;
        }

        public Object getUnit() {
            return unit;
        }

        public void setUnit(Object unit) {
            this.unit = unit;
        }
    }

    public List<ConfigMapRateLimitDescriptor> getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(List<ConfigMapRateLimitDescriptor> descriptors) {
        this.descriptors = descriptors;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
