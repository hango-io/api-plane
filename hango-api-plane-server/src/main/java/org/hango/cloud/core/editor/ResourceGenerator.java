package org.hango.cloud.core.editor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于JsonPath的资源转换工具
 * 1. 提供obj, json, yaml 三者互相转换的静态方法
 * 2. 可以对json基于JsonPath语法，进行修改裁剪，然后再以json或yaml或obj转出
 *
 **/
public class ResourceGenerator implements Editor {

    protected static EditorContext defaultContext = new EditorContext(new ObjectMapper(), new YAMLMapper(), Configuration.defaultConfiguration());

    public static void configDefaultContext(EditorContext editorContext) {
        defaultContext = editorContext;
    }

    protected EditorContext editorContext;
    protected String originalJson;
    protected DocumentContext jsonContext;

    protected ResourceGenerator(Object resource, ResourceType type, EditorContext editorContext) {
        if (Objects.isNull(resource) || Objects.isNull(type) || Objects.isNull(editorContext)) {
            throw new ApiPlaneException("ResourceGenerator's construction parameter cannot be empty");
        }
        this.editorContext = editorContext;
        switch (type) {
            case JSON:
                this.originalJson = (String) resource;
                break;
            case YAML:
                this.originalJson = yaml2json((String) resource, editorContext);
                break;
            case OBJECT:
                this.originalJson = obj2json(resource, editorContext);
                break;
        }
        this.jsonContext = JsonPath.using(editorContext.configuration()).parse(originalJson);
    }

    public static ResourceGenerator newInstance(Object resource, ResourceType type, EditorContext editorContext) {
        return new ResourceGenerator(resource, type, editorContext);
    }

    public static ResourceGenerator newInstance(Object resource, ResourceType type) {
        return new ResourceGenerator(resource, type, defaultContext);
    }

    public static ResourceGenerator newInstance(Object resource) {
        return new ResourceGenerator(resource, ResourceType.JSON, defaultContext);
    }

    /**
     * 是否包含path
     *
     * @param path   jsonpath expression
     * @param filter 配合jsonpath expression筛选path
     * @return
     */
    @Override
    public boolean contain(String path, Predicate... filter) {
        Object result = jsonContext.read(path, filter);
        if (result instanceof List) {
            return ((List) result).size() != 0;
        }
        if (result instanceof Map) {
            return ((Map) result).size() != 0;
        }
        return jsonContext.read(path, filter) != null;
    }

    /**
     * get value
     * 返回值类型有三种:
     * 1. List : 当path中包含[]，即便结果只有一个value，也是以List形式返回
     * 2. Map : 当path中不包含[]，且path选择的是一个json对象，就以Map返回
     * 3. String : 当path中不包含[]，且path选择的是一个string对象，就以String返回
     *
     * @param path   jsonpath expression
     * @param filter 配合jsonpath expression筛选path
     * @return
     */
    @Override
    public <T> T getValue(String path, Predicate... filter) {
        return (T) jsonContext.read(path, filter);
    }

    /**
     * 当get value的结果是Map类型时，尝试用type进行反序列化
     *
     * @param path   jsonpath expression
     * @param type   反序列化类型
     * @param filter 配合jsonpath expression筛选path
     * @return
     */
    @Override
    public <T> T getValue(String path, Class<T> type, Predicate... filter) {
        return jsonContext.read(path, type, filter);
    }

    /**
     * 插入一个value到数组
     * 例如:
     * json: {"matcher": []}
     * path: $.matcher
     * value: abc
     * 执行结果为:
     * {"matcher": ["abc"]}
     *
     * @param path   jsonpath expression
     * @param value  value
     * @param filter 配合jsonpath expression筛选path
     */
    @Override
    public synchronized void addElement(String path, Object value, Predicate... filter) {
        jsonContext.add(path, value, filter);
    }

    /**
     * 从数组中移除一个value
     * 例如:
     * json: {"matcher": ["abc"]}
     * path: $.matcher[0]
     * 执行结果为:
     * {"matcher": []}
     *
     * @param path   jsonpath expression
     * @param filter 配合jsonpath expression筛选path
     */
    @Override
    public synchronized void removeElement(String path, Predicate... filter) {
        jsonContext.delete(path, filter);
    }

    /**
     * 覆盖path的value
     * 例如:
     * json: {"matcher": ["abc"]}
     * path: $.matcher[0]
     * value: def
     * 执行结果为:
     * {"matcher": ["def"]}
     *
     * @param path   jsonpath expression
     * @param value  可以是对象或者string
     * @param filter 配合jsonpath expression筛选path
     */
    @Override
    public void updateValue(String path, Object value, Predicate... filter) {
        jsonContext.set(path, value, filter);
    }

    /**
     * 添加或更新一个value
     * 例如:
     * json: {"matcher": ["abc"]}
     * path: $
     * key: "action"
     * value: "def"
     * 执行结果为:
     * {"matcher": ["def"], "action":"def"}
     *
     * @param path   jsonpath expression
     * @param key    插入或更新的key
     * @param value  插入或更新的value，可以是对象或string
     * @param filter 配合jsonpath expression筛选path
     */
    @Override
    public void createOrUpdateValue(String path, String key, Object value, Predicate... filter) {
        jsonContext.put(path, key, value, filter);
    }

    /**
     * 将json串作为一个对象而不是String插入到数组
     * <p>
     * 例如:
     * json: {"matcher": []}
     * path: $.matcher
     * value: {"key": "abc"}
     * 执行结果为:
     * {"matcher": [{"key": "abc"}]}
     *
     * @param path   jsonpath expression
     * @param json   插入的json串
     * @param filter 配合jsonpath expression筛选path
     */
    @Override
    public ResourceGenerator addJsonElement(String path, String json, Predicate... filter) {
        if (json.startsWith("[")) {
            addElement(path, json2obj(json, List.class, editorContext), filter);
        } else if (json.startsWith("{")) {
            addElement(path, json2obj(json, Map.class, editorContext), filter);
        } else {
            addElement(path, json, filter);
        }
        return this;
    }


    /**
     * 将json串作为一个对象进行更新或添加对象
     * <p>
     * 例如:
     * json: {"matcher": ["abc"]}
     * path: $
     * key: "action"
     * value: {"key": "abc"}
     * 执行结果为:
     * {"matcher": ["def"], "action":{"key": "abc"}}
     *
     * @param path   jsonpath expression
     * @param json   插入的json串
     * @param filter 配合jsonpath expression筛选path
     */
    @Override
    public ResourceGenerator createOrUpdateJson(String path, String key, String json, Predicate... filter) {
        if (json.startsWith("[")) {
            createOrUpdateValue(path, key, json2obj(json, List.class, editorContext), filter);
        } else if (json.startsWith("{")) {
            createOrUpdateValue(path, key, json2obj(json, Map.class, editorContext), filter);
        } else {
            createOrUpdateValue(path, key, json, filter);
        }
        return this;
    }

    /**
     * 转出为json
     *
     * @return
     */
    @Override
    public synchronized String jsonString() {
        return jsonContext.jsonString();
    }

    /**
     * 转出为yaml
     *
     * @return
     */
    @Override
    public synchronized String yamlString() {
        return json2yaml(jsonContext.jsonString(), editorContext);
    }

    /**
     * 转出为object
     *
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T object(Class<T> type) {
        return jsonContext.read("$", type);
    }

    public static String yaml2json(String yaml) {
        return yaml2json(yaml, defaultContext);
    }

    public static String json2yaml(String json) {
        return json2yaml(json, defaultContext);
    }

    public static String obj2json(Object obj) {
        return obj2json(obj, defaultContext);
    }

    public static <T> T json2obj(String json, Class<T> type) {
        return json2obj(json, type, defaultContext);
    }

    public static <T> T yaml2obj(String yaml, Class<T> type) {
        return yaml2obj(yaml, type, defaultContext);
    }

    public static String prettyJson(String json) {
        if (StringUtils.isEmpty(json)) return json;
        return prettyJson(json, defaultContext);
    }

    public static String yaml2json(String yaml, EditorContext editorContext) {
        try {
            Object obj = editorContext.yamlMapper().readValue(yaml, Object.class);
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }

    }

    public static String json2yaml(String json, EditorContext editorContext) {
        try {
            Object obj = editorContext.jsonMapper().readValue(json, Object.class);
            return editorContext.yamlMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static String obj2json(Object obj, EditorContext editorContext) {
        try {
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static <T> T json2obj(String json, Class<T> type, EditorContext editorContext) {
        try {
            return editorContext.jsonMapper().readValue(json, type);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static <T> T yaml2obj(String yaml, Class<T> type, EditorContext editorContext) {
        return json2obj(yaml2json(yaml, editorContext), type, editorContext);
    }

    public static String prettyJson(String json, EditorContext editorContext) {
        try {
            return editorContext.jsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json2obj(json, Object.class, editorContext));
        } catch (JsonProcessingException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }
}
