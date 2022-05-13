package org.hango.cloud.meta;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/1
 **/
public class HealthServiceSubset {

    private String host;

    private String subset;

    public HealthServiceSubset(String host) {
        this.host = host;
    }

    public HealthServiceSubset(String host, String subset) {
        this.host = host;
        this.subset = subset;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }
}
