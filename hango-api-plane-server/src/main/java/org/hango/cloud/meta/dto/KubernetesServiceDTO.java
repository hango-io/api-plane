package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/12/13
 **/
public class KubernetesServiceDTO {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Namespace")
    private String namespace;

    @JsonProperty("Domain")
    private String domain;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Ports")
    private List<ServicePort> ports;


    @JsonProperty("ClusterIP")
    private String clusterIP;



    public static class ServicePort{
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Port")
        private Integer port;

        @JsonProperty("NodePort")
        private Integer nodePort;

        @JsonProperty("TargetPort")
        private Integer targetPort;

        @JsonProperty("Protocol")
        private String protocol;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getNodePort() {
            return nodePort;
        }

        public void setNodePort(Integer nodePort) {
            this.nodePort = nodePort;
        }

        public Integer getTargetPort() {
            return targetPort;
        }

        public void setTargetPort(Integer targetPort) {
            this.targetPort = targetPort;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDomain() {
        return name + "." + namespace + ".svc.cluster.local";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ServicePort> getPorts() {
        return ports;
    }

    public void setPorts(List<ServicePort> ports) {
        this.ports = ports;
    }

    public String getClusterIP() {
        return clusterIP;
    }

    public void setClusterIP(String clusterIP) {
        this.clusterIP = clusterIP;
    }
}
