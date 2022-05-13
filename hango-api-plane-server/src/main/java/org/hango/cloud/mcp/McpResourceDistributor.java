package org.hango.cloud.mcp;

import istio.mcp.nsf.SnapshotOuterClass;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/13
 **/
public interface McpResourceDistributor {
    void setSnapshot(SnapshotOuterClass.Snapshot snapshot);

    void clearSnapshot();
}
