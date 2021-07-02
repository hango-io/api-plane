package org.hango.cloud.configuration.ext;

import org.springframework.beans.factory.annotation.Value;

public class MeshConfig {

    @Value("${meshProjectKey:hango.org/project}")
    private String projectKey;
    @Value("${meshVersionKey:hango.org/version}")
    private String versionKey;
    @Value("${meshAppKey:hango.org/app}")
    private String appKey;
    @Value("${selectorAppKey:hango.org/app}")
    private String selectorAppKey;

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
}
