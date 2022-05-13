package org.hango.cloud.core.plugin.processor;


import org.hango.cloud.core.plugin.FragmentHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/5
 **/
public interface SchemaProcessor<T> {
    // processor名，对应label #@processor
    String getName();

    FragmentHolder process(String plugin, T serviceInfo);

    // 默认不做合并
    default List<FragmentHolder> process(List<String> plugins, T serviceInfo) {
        return plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
    }
}
