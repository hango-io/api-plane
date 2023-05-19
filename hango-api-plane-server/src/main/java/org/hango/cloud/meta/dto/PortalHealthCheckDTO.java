package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class PortalHealthCheckDTO {

    @JsonProperty(value = "Path")
    @NotNull(message = "path")
    private String path;

    @JsonProperty(value = "Timeout")
    @Min(value = 0, message = "timeout")
    private Long timeout;

    @JsonProperty(value = "ExpectedStatuses")
//    @NotNull(message = "expected statuses")
    private List<Integer> expectedStatuses;

    @JsonProperty(value = "HealthyInterval")
    @Min(value = 0, message = "healthy interval")
    private Long healthyInterval;

    @JsonProperty(value = "HealthyThreshold")
    @Min(value = 0, message = "healthy threshold")
    @Max(value = 100, message = "healthy threshold")
    private Integer healthyThreshold;

    @JsonProperty(value = "UnhealthyInterval")
    @Min(value = 0, message = "unhealthy interval")
    private Long unhealthyInterval;

    @JsonProperty(value = "UnhealthyThreshold")
    @Min(value = 0, message = "unhealthy threshold")
    @Max(value = 100, message = "unhealthy threshold")
    private Integer unhealthyThreshold;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public List<Integer> getExpectedStatuses() {
        return expectedStatuses;
    }

    public void setExpectedStatuses(List<Integer> expectedStatuses) {
        this.expectedStatuses = expectedStatuses;
    }

    public Long getHealthyInterval() {
        return healthyInterval;
    }

    public void setHealthyInterval(Long healthyInterval) {
        this.healthyInterval = healthyInterval;
    }

    public Integer getHealthyThreshold() {
        return healthyThreshold;
    }

    public void setHealthyThreshold(Integer healthyThreshold) {
        this.healthyThreshold = healthyThreshold;
    }

    public Long getUnhealthyInterval() {
        return unhealthyInterval;
    }

    public void setUnhealthyInterval(Long unhealthyInterval) {
        this.unhealthyInterval = unhealthyInterval;
    }

    public Integer getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }
}
