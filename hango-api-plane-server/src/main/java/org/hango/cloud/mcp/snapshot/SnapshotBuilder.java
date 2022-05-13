package org.hango.cloud.mcp.snapshot;

import istio.mcp.nsf.SnapshotOuterClass;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/15
 **/
public interface SnapshotBuilder {
    SnapshotOuterClass.Snapshot build();
}
