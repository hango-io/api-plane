package org.hango.cloud.meta;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/7
 **/
public class PairMatch {

    private String key;

    private String value;

    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PairMatch(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public PairMatch() {
    }
}
