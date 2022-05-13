package org.hango.cloud.core.template;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/9
 **/
public class TemplateParams {

    private TemplateParams parent;

    private Map<String, Object> params = new HashMap<>();

    private static TemplateParams EMPTY = new TemplateParams();

    public static TemplateParams instance() {
        return new TemplateParams();
    }

    public static TemplateParams empty() {
        return EMPTY;
    }

    public TemplateParams setParent(TemplateParams parent) {
        this.parent = parent;
        return this;
    }

    private TemplateParams() {}

    public TemplateParams put(String k, Object v) {
        params.put(k, v);
        return this;
    }

    public Object get(String k) {
        return params.get(k);
    }

    public Map<String, Object> output() {
        Map<String, Object> p = new HashMap<>();
        if (parent != null) {
            p.putAll(parent.output());
        }
        p.putAll(params);
        return p;
    }


}
