package org.hango.cloud.mcp;

import istio.mcp.v1alpha1.Mcp;

public class WatchResponse {
    private String snapshotVersion;
    private Mcp.Resources resource;

    public WatchResponse(String snapshotVersion, Mcp.Resources resource) {
        this.snapshotVersion = snapshotVersion;
        this.resource = resource;
    }

    public String getSnapshotVersion() {
        return snapshotVersion;
    }

    public void setSnapshotVersion(String snapshotVersion) {
        this.snapshotVersion = snapshotVersion;
    }

    public Mcp.Resources getResource() {
        return resource;
    }

    public void setResource(Mcp.Resources resource) {
        this.resource = resource;
    }
}
