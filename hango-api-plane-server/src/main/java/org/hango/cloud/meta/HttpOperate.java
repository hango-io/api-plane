package org.hango.cloud.meta;

/**
 * @Author zhufengwei
 * @Date 2023/8/15
 */
public class HttpOperate {
    /**
     * 操作对象类型 header/query
     */
    private String type;

    /**
     * 操作元素key
     */
    private String key;

    /**
     * 操作元素value
     */
    private String value;

    /**
     * 操作类型 add/delete
     */
    private String action;

    public static HttpOperate of(String type, String action, String key, String value){
        HttpOperate httpOperate = new HttpOperate();
        httpOperate.setType(type);
        httpOperate.setKey(key);
        httpOperate.setValue(value);
        httpOperate.setAction(action);
        return httpOperate;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
