package org.hango.cloud.mcp;

public interface McpResourceWatcher {
    void watch(Connection connection, String collection);

    void release(Connection connection);
}
