package org.hango.cloud.meta;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class WhiteList {

    private String service;
    private String namespace;

    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public String getFullService() {
        return String.format("%s.%s.svc.cluster.local", service, namespace);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSourcesNamespace() {
        return getNamespace();
    }

}
