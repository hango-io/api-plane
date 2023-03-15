package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public class PortalLoadBalancerDTO {

    @JsonProperty(value = "Simple")
    private String simple;

    @JsonProperty(value = "SlowStartWindow")
    private Integer slowStartWindow;

    @JsonProperty(value = "ConsistentHash")
    private ConsistentHashDTO consistentHashDTO;

    @JsonProperty(value = "LocalitySetting")
    private LocalitySettingDTO localitySetting;

    public String getSimple() {
        return simple;
    }

    public void setSimple(String simple) {
        this.simple = simple;
    }

    public ConsistentHashDTO getConsistentHashDTO() {
        return consistentHashDTO;
    }

    public void setConsistentHashDTO(ConsistentHashDTO consistentHashDTO) {
        this.consistentHashDTO = consistentHashDTO;
    }

    public LocalitySettingDTO getLocalitySetting() {
        return localitySetting;
    }

    public void setLocalitySetting(LocalitySettingDTO localitySetting) {
        this.localitySetting = localitySetting;
    }

    public Integer getSlowStartWindow() {
        return slowStartWindow;
    }

    public void setSlowStartWindow(Integer slowStartWindow) {
        this.slowStartWindow = slowStartWindow;
    }

    public static class ConsistentHashDTO {

        @JsonProperty(value = "HttpHeaderName")
        private String httpHeaderName;

        @JsonProperty(value = "UseSourceIp")
        private Boolean useSourceIp;

        @JsonProperty(value = "HttpCookie")
        @Valid
        private HttpCookieDTO httpCookie;

        public String getHttpHeaderName() {
            return httpHeaderName;
        }

        public void setHttpHeaderName(String httpHeaderName) {
            this.httpHeaderName = httpHeaderName;
        }

        public Boolean getUseSourceIp() {
            return useSourceIp;
        }

        public void setUseSourceIp(Boolean useSourceIp) {
            this.useSourceIp = useSourceIp;
        }

        public HttpCookieDTO getHttpCookie() {
            return httpCookie;
        }

        public void setHttpCookie(HttpCookieDTO httpCookie) {
            this.httpCookie = httpCookie;
        }

        public static class HttpCookieDTO {

            @JsonProperty(value = "Name")
            @NotEmpty(message = "http cookie name")
            private String name;

            @JsonProperty(value = "Path")
            private String path;

            @JsonProperty(value = "TTL")
            @NotEmpty(message = "http cookie ttl")
            private Integer ttl;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public Integer getTtl() {
                return ttl;
            }

            public void setTtl(Integer ttl) {
                this.ttl = ttl;
            }
        }
    }
}
