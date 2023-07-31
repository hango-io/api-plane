package org.hango.cloud.core.plugin.processor;


import org.hango.cloud.core.plugin.FragmentHolder;

public interface SchemaProcessor<T> {
    // processor名，对应label #@processor
    String getName();

    FragmentHolder process(String plugin, T serviceInfo);
}
