package org.hango.cloud.mcp.status;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/5/6
 **/
public interface StatusNotifier {
    void notifyStatus(String key);

    void notifyStatus(String key, String value);

    void notifyStatus(String key, ValueGenerator generator);

    @FunctionalInterface
    interface ValueGenerator {
        String generate(String key);
    }
}
