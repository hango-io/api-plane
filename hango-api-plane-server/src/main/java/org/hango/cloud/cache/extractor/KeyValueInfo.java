package org.hango.cloud.cache.extractor;

public class KeyValueInfo {

    private String key;
    private String value;

    public KeyValueInfo(String pattern, String value) {
        this.key = pattern;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
