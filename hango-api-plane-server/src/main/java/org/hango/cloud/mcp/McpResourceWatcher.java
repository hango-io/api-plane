package org.hango.cloud.mcp;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/13
 **/
public interface McpResourceWatcher {
    void watch(Connection connection, String collection);

    void release(Connection connection);
}
