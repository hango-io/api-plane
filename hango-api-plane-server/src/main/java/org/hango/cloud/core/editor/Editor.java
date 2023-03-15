package org.hango.cloud.core.editor;

import com.jayway.jsonpath.Predicate;

public interface Editor {
    boolean contain(String path, Predicate... filter);

    <T> T getValue(String path, Predicate... filter);

    <T> T getValue(String path, Class<T> type, Predicate... filter);

    void addElement(String path, Object value, Predicate... filter);

    void removeElement(String path, Predicate... filter);

    void updateValue(String path, Object value, Predicate... filter);

    void createOrUpdateValue(String path, String key, Object value, Predicate... filter);

    Editor addJsonElement(String path, String json, Predicate... filter);

    Editor createOrUpdateJson(String path, String key, String json, Predicate... filter);

    /**
     * 导出为json
     *
     * @return
     */
    String jsonString();

    /**
     * 导出为yaml
     *
     * @return
     */
    String yamlString();

    /**
     * 导出为object
     *
     * @param type
     * @param <T>
     * @return
     */
    <T> T object(Class<T> type);
}
