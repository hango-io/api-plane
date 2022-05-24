package org.hango.cloud.mcp.status;

public interface StatusNotifier {
    void notifyStatus(String key);

    void notifyStatus(String key, String value);

    void notifyStatus(String key, ValueGenerator generator);

    @FunctionalInterface
    interface ValueGenerator {
        String generate(String key);
    }
}
