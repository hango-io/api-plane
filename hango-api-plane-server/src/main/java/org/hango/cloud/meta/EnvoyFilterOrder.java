package org.hango.cloud.meta;


import java.util.List;

/**
 * @author xin li
 * @date 2022/5/16 10:09
 */
public class EnvoyFilterOrder {

    private String name;

    private String namespace;

    private Integer portNumber;

    private String gwCluster;

    private List<String> configPatches;

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getConfigPatches() {
        return configPatches;
    }

    public void setConfigPatches(List<String> configPatches) {
        this.configPatches = configPatches;
    }

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }
}
