package org.hango.cloud.mcp;

import istio.mcp.nsf.SnapshotOuterClass;

public interface McpResourceDistributor {
    void setSnapshot(SnapshotOuterClass.Snapshot snapshot);

    void clearSnapshot();
}
