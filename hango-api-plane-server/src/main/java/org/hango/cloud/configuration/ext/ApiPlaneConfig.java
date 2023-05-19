package org.hango.cloud.configuration.ext;

import org.hango.cloud.util.Const;

public class ApiPlaneConfig {

    private String nsfMetaUrl;

    private String startInformer = Const.OPTION_TRUE;

    private String daemonSetName;

    private String daemonSetNamespace;

    private String daemonSetPort;

    public String getDaemonSetName() {
        return daemonSetName;
    }

    public void setDaemonSetName(String daemonSetName) {
        this.daemonSetName = daemonSetName;
    }

    public String getDaemonSetNamespace() {
        return daemonSetNamespace;
    }

    public void setDaemonSetNamespace(String daemonSetNamespace) {
        this.daemonSetNamespace = daemonSetNamespace;
    }

    public String getDaemonSetPort() {
        return daemonSetPort;
    }

    public void setDaemonSetPort(String daemonSetPort) {
        this.daemonSetPort = daemonSetPort;
    }

    public String getStartInformer() {
        return startInformer;
    }

    public void setStartInformer(String startInformer) {
        this.startInformer = startInformer;
    }

    public String getNsfMetaUrl() {
        return nsfMetaUrl;
    }

    public void setNsfMetaUrl(String nsfMetaUrl) {
        this.nsfMetaUrl = nsfMetaUrl;
    }
}
