package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/12/5
 */
public class KubernetesGatewayDTO {

    /**
     * 名称
     */
    @JsonProperty("Name")
    private String name;


    /**
     * 所属项目id
     */
    @JsonProperty("ProjectId")
    private String projectId;



    @JsonProperty("Protocol")
    private String protocol;

    /**
     * 监听域名
     */
    @JsonProperty("Host")
    private String host;

    /**
     * 监听域名
     */
    @JsonProperty("RouteHosts")
    private List<String> routeHosts;


    /**
     * 监听端口
     */
    @JsonProperty("Port")
    private int port;



    /**
     * 配置详细内容
     */
    @JsonProperty("Content")
    private String content;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getRouteHosts() {
        return routeHosts;
    }

    public void setRouteHosts(List<String> routeHosts) {
        this.routeHosts = routeHosts;
    }
}
