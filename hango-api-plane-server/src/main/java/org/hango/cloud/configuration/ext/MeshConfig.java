package org.hango.cloud.configuration.ext;

import org.springframework.beans.factory.annotation.Value;

public class MeshConfig {

    @Value("${meshProjectKey:nsf.skiff.netease.com/project}")
    private String projectKey;
    @Value("${meshVersionKey:nsf.skiff.netease.com/version}")
    private String versionKey;
    @Value("${meshAppKey:nsf.skiff.netease.com/app}")
    private String appKey;
    @Value("${selectorAppKey:nsf.skiff.netease.com/app}")
    private String selectorAppKey;
    @Value("${trafficMarkLabel:nsf.skiff.netease.com/mark}")
    private String trafficMarkLabel;

    public String getSelectorAppKey() {
        return selectorAppKey;
    }

    public void setSelectorAppKey(String selectorAppKey) {
        this.selectorAppKey = selectorAppKey;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getVersionKey() {
        return versionKey;
    }

    public void setVersionKey(String versionKey) {
        this.versionKey = versionKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getTrafficMarkLabel() {
        return trafficMarkLabel;
    }

    public void setTrafficMarkLabel(String trafficMarkLabel) {
        this.trafficMarkLabel = trafficMarkLabel;
    }
}
