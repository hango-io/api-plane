package org.hango.cloud.meta;

import com.fasterxml.jackson.annotation.JsonProperty;


public class PodStatus {

    @JsonProperty(value = "PodName")
    private String podName;

    @JsonProperty(value = "CurrentVersion")
    private String currentVersion;

    @JsonProperty(value = "ExpectedVersion")
    private String expectedVersion;

    @JsonProperty(value = "LastUpdateTime")
    private String lastUpdateTime;

    @JsonProperty(value = "StatusCode")
    private Integer statusCode;

    @JsonProperty(value = "StatusMessage")
    private String statusMessage;

    public PodStatus(String podName, String currentVersion, String expectedVersion ,String lastUpdateTime, Integer statusCode, String statusMessage) {
        this.podName = podName;
        this.currentVersion = currentVersion;
        this.expectedVersion = expectedVersion;
        this.lastUpdateTime = lastUpdateTime;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public PodStatus(String podName) {
        this.podName = podName;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(String expectedVersion) {
        this.expectedVersion = expectedVersion;
    }

}
