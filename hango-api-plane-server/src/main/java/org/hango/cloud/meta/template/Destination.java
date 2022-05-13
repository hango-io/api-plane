package org.hango.cloud.meta.template;


/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/6/14
 **/
public class Destination {

    private String subset;

    private Integer weight;

    private String host;

    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
