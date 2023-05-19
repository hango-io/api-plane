package org.hango.cloud.meta;

import org.hango.cloud.k8s.K8sResourceApiEnum;

/**
 * @Author zhufengwei
 * @Date 2023/4/4
 */
public class CustomResource<T, L> {
    //资源定义
    private K8sResourceApiEnum kind;
    //资源类型
    private Class<T> apiTypeClass;
    //资源列表类型
    private Class<L> apiListTypeClass;

    public K8sResourceApiEnum getKind() {
        return kind;
    }

    public void setKind(K8sResourceApiEnum kind) {
        this.kind = kind;
    }

    public Class<T> getApiTypeClass() {
        return apiTypeClass;
    }

    public void setApiTypeClass(Class<T> apiTypeClass) {
        this.apiTypeClass = apiTypeClass;
    }

    public Class<L> getApiListTypeClass() {
        return apiListTypeClass;
    }

    public void setApiListTypeClass(Class<L> apiListTypeClass) {
        this.apiListTypeClass = apiListTypeClass;
    }
}
