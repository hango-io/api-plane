package org.hango.cloud.mcp.snapshot;

import istio.mcp.nsf.SnapshotOuterClass;

public interface SnapshotBuilder {
    SnapshotOuterClass.Snapshot build();
}
