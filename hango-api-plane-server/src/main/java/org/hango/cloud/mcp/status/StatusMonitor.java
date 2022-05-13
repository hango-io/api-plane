package org.hango.cloud.mcp.status;

import java.util.function.BiConsumer;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public interface StatusMonitor {
    void registerHandler(String key, BiConsumer<Event, Status.Property> handle);

    void start();

    void shutdown();

    enum Event {
        ADD, UPDATE, DELETE
    }
}