package org.hango.cloud.core.gateway;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/28
 **/
public class RawResourceContainer {

    List<FragmentWrapper> virtualServices = new ArrayList<>();
    List<FragmentWrapper> sharedConfigs = new ArrayList<>();
    List<FragmentWrapper> gatewayPlugins = new ArrayList<>();

    public void add(FragmentHolder holder) {

        if (holder == null) return;

        if (holder.getVirtualServiceFragment() != null) {
            virtualServices.add(holder.getVirtualServiceFragment());
        }
        if (holder.getSharedConfigFragment() != null) {
            sharedConfigs.add(holder.getSharedConfigFragment());
        }
        if (holder.getGatewayPluginsFragment() != null) {
            gatewayPlugins.add(holder.getGatewayPluginsFragment());
        }
    }

    public void add(List<FragmentHolder> holders) {
        if (CollectionUtils.isEmpty(holders)) return;
        holders.stream().forEach(h -> add(h));
    }

    public List<FragmentWrapper> getVirtualServices() {
        return virtualServices;
    }

    public List<FragmentWrapper> getSharedConfigs() {
        return sharedConfigs;
    }

    public List<FragmentWrapper> getGatewayPlugins() {
        return gatewayPlugins;
    }
}
