package org.hango.cloud.meta.template;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;


public class NsfExtra {

    private String host;

    private UrlMatch uri;

    private List<Header> headers;

    private List<Destination> destinations;

    @Min(value = 0, message = "percent")
    @Max(value = 100, message = "percent")
    private Integer percent;

    private String fixedDelay;

    private Integer httpStatus;

    private Integer consecutiveErrors = 5;

    private String interval = "10s";

    private String baseEjectionTime = "30s";

    @Min(value = 1, message = "maxEjectionPercent")
    @Max(value = 100, message = "maxEjectionPercent")
    private Integer maxEjectionPercent = 10;

    @Pattern(regexp = "(ROUND_ROBIN|LEASE_CONN|RANDOM|PASSTHROUGH)", message = "simple")
    private String simple;

    private List<String> hosts;

    @Valid
    private List<Service> targetList;

    @Min(value = 0, message = "outWeight")
    @Max(value = 100, message = "outWeight")
    private Integer outWeight;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
    }

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public String getFixedDelay() {
        return fixedDelay;
    }

    public void setFixedDelay(String fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Integer getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(Integer consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(String baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    public Integer getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(Integer maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public String getSimple() {
        return simple;
    }

    public void setSimple(String simple) {
        this.simple = simple;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<Service> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<Service> targetList) {
        this.targetList = targetList;
    }

    public UrlMatch getUri() {
        return uri;
    }

    public void setUri(UrlMatch uri) {
        this.uri = uri;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public Integer getOutWeight() {
        return outWeight;
    }

    public void setOutWeight(Integer outWeight) {
        this.outWeight = outWeight;
    }


    public static final class NsfExtraBuilder {
        private String host;
        private UrlMatch uri;
        private List<Header> headers;
        private List<Destination> destinations;
        private Integer percent;
        private String fixedDelay;
        private Integer httpStatus;
        private Integer consecutiveErrors;
        private String interval;
        private String baseEjectionTime;
        private Integer maxEjectionPercent;
        private String simple;
        private List<String> hosts;
        private List<Service> targetList;
        private Integer outWeight;

        private NsfExtraBuilder() {
        }

        public static NsfExtraBuilder aNsfExtra() {
            return new NsfExtraBuilder();
        }

        public NsfExtraBuilder withHost(String host) {
            this.host = host;
            return this;
        }

        public NsfExtraBuilder withUri(UrlMatch uri) {
            this.uri = uri;
            return this;
        }

        public NsfExtraBuilder withHeaders(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        public NsfExtraBuilder withDestinations(List<Destination> destinations) {
            this.destinations = destinations;
            return this;
        }

        public NsfExtraBuilder withPercent(Integer percent) {
            this.percent = percent;
            return this;
        }

        public NsfExtraBuilder withFixedDelay(String fixedDelay) {
            this.fixedDelay = fixedDelay;
            return this;
        }

        public NsfExtraBuilder withHttpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public NsfExtraBuilder withConsecutiveErrors(Integer consecutiveErrors) {
            this.consecutiveErrors = consecutiveErrors;
            return this;
        }

        public NsfExtraBuilder withInterval(String interval) {
            this.interval = interval;
            return this;
        }

        public NsfExtraBuilder withBaseEjectionTime(String baseEjectionTime) {
            this.baseEjectionTime = baseEjectionTime;
            return this;
        }

        public NsfExtraBuilder withMaxEjectionPercent(Integer maxEjectionPercent) {
            this.maxEjectionPercent = maxEjectionPercent;
            return this;
        }

        public NsfExtraBuilder withSimple(String simple) {
            this.simple = simple;
            return this;
        }

        public NsfExtraBuilder withHosts(List<String> hosts) {
            this.hosts = hosts;
            return this;
        }

        public NsfExtraBuilder withTargetList(List<Service> targetList) {
            this.targetList = targetList;
            return this;
        }

        public NsfExtraBuilder withOutWeight(Integer outWeight) {
            this.outWeight = outWeight;
            return this;
        }

        public NsfExtra build() {
            NsfExtra nsfExtra = new NsfExtra();
            nsfExtra.setHost(host);
            nsfExtra.setUri(uri);
            nsfExtra.setHeaders(headers);
            nsfExtra.setDestinations(destinations);
            nsfExtra.setPercent(percent);
            nsfExtra.setFixedDelay(fixedDelay);
            nsfExtra.setHttpStatus(httpStatus);
            nsfExtra.setConsecutiveErrors(consecutiveErrors);
            nsfExtra.setInterval(interval);
            nsfExtra.setBaseEjectionTime(baseEjectionTime);
            nsfExtra.setMaxEjectionPercent(maxEjectionPercent);
            nsfExtra.setSimple(simple);
            nsfExtra.setHosts(hosts);
            nsfExtra.setTargetList(targetList);
            nsfExtra.setOutWeight(outWeight);
            return nsfExtra;
        }
    }
}
