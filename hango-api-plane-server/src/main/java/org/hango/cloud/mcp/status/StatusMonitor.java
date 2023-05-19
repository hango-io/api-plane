package org.hango.cloud.mcp.status;

import java.util.function.BiConsumer;

public interface StatusMonitor {
    void registerHandler(String key, BiConsumer<Event, Status.Property> handle);

    void start();

    void shutdown();

    enum Event {
        ADD, UPDATE, DELETE
    }
}