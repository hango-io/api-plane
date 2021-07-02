package org.hango.cloud.service;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.Plugin;
import org.hango.cloud.meta.ServiceInfo;

import java.util.List;
import java.util.Map;

public interface PluginService {
    Plugin getPlugin(String name);

    Map<String, Plugin> getPlugins();

    String getSchema(String path);

    List<FragmentHolder> processPlugin(List<String> plugins, ServiceInfo serviceInfo);

    List<String> extractService(List<String> plugins);
}
