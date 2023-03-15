package org.hango.cloud.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:37
**/
public interface ResourceCache {

    /**
     * 获取资源类型
     * @param kind 资源类型
     * @return 资源列表
     */
    List<HasMetadata> getResource(String kind);

    /**
     * 获取资源类型
     * @param kind 资源类型
     * @return 资源列表
     */
    List<HasMetadata> getResourceByName(String kind, String name);

    /**
     * 获取资源类型
     * @param gateway 网关名称
     * @param kind 资源类型
     * @return 资源列表
     */
    List<HasMetadata> getResource(String gateway, String kind);

    /**
     * 获取资源名称
     * @param kind 资源类型
     * @return 资源列表
     */
    List<String> getResourceName(String kind);

}
