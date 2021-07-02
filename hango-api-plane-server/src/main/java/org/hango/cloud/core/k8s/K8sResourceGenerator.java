package org.hango.cloud.core.k8s;

import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.PathExpressionEnum;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.util.exception.ApiPlaneException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class K8sResourceGenerator extends ResourceGenerator {


    private K8sResourceGenerator(Object resource, ResourceType type, EditorContext editorContext) {
        super(resource, type, editorContext);
    }

    public static K8sResourceGenerator newInstance(String json) {
        return new K8sResourceGenerator(json, ResourceType.JSON, defaultContext);
    }

    public static K8sResourceGenerator newInstance(Object resource, ResourceType type) {
        return new K8sResourceGenerator(resource, type, defaultContext);
    }

    public String getName() {
        return getValue(PathExpressionEnum.GET_NAME.translate());
    }

    public String getNamespace() {
        if (contain(PathExpressionEnum.GET_NAMESPACE.translate())) {
            return getValue(PathExpressionEnum.GET_NAMESPACE.translate());
        } else {
            return "default";
        }
    }

    public String getKind() {
        return getValue(PathExpressionEnum.GET_KIND.translate());
    }

    public Object getSpec() {
        return getValue(PathExpressionEnum.GET_SPEC.translate());
    }

    public String getApiVersion() {
        return getValue(PathExpressionEnum.GET_APIVERSION.translate());
    }

    public String getResourceVersion() {
        return getValue(PathExpressionEnum.GET_RESOURCEVERSION.translate());
    }

    public String getCreateTimestamp() {
        return getValue(PathExpressionEnum.GET_CREATETIME.translate());
    }

    public void setName(String name) {
        updateValue(PathExpressionEnum.GET_NAME.translate(), name);
    }

    public void setNamespace(String namespace) {
        updateValue(PathExpressionEnum.GET_NAMESPACE.translate(), namespace);
    }

    public void setKind(String kind) {
        updateValue(PathExpressionEnum.GET_KIND.translate(), kind);
    }

    public void setApiVersion(String apiVersion) {
        updateValue(PathExpressionEnum.GET_APIVERSION.translate(), apiVersion);
    }

    public Map<String, String> getLabels() {
        return getValue(PathExpressionEnum.GET_LABEL.translate());
    }

    public Map<String, String> getAnnotations() {
        return getValue(PathExpressionEnum.GET_ANNOTATIONS.translate());
    }

    public void setResourceVersion(String resourceVersion) {
        updateValue(PathExpressionEnum.GET_RESOURCEVERSION.translate(), resourceVersion);
    }

    public boolean isList() {
        Pattern pattern = Pattern.compile("(.*)List$");
        return pattern.matcher(getKind()).find();
    }

    public List<String> items() {
        if (!isList()) {
            throw new ApiPlaneException("Cant convert Object to List Type.");
        }
        List<String> ret = new ArrayList<>();
        List objs = getValue(PathExpressionEnum.GET_ITEMS.translate());
        objs.forEach(obj -> ret.add(ResourceGenerator.newInstance(obj, ResourceType.OBJECT, editorContext).jsonString()));
        return ret;
    }

    public <T> List<T> items(Class<T> itemsType) {
        if (!isList()) {
            throw new ApiPlaneException("Cant convert Object to List Type.");
        }
        List<T> ret = new ArrayList<>();
        List<String> jsons = items();
        jsons.forEach(json -> ret.add(ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext).object(itemsType)));
        return ret;
    }
}
