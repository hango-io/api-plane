package org.hango.cloud.core.gateway;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.FragmentWrapper;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class RawResourceContainer {

    private List<FragmentWrapper> gatewayPlugins = new ArrayList<>();
    private List<FragmentWrapper> smartLimiters = new ArrayList<>();

    public void add(FragmentHolder holder) {

        if (holder == null) return;

        if (holder.getGatewayPluginsFragment() != null) {
            gatewayPlugins.add(holder.getGatewayPluginsFragment());
        }

        if (holder.getSmartLimiterFragment() != null) {
            smartLimiters.addAll(holder.getSmartLimiterFragment());
        }
    }

    public void add(List<FragmentHolder> holders) {
        if (CollectionUtils.isEmpty(holders)) return;
        holders.forEach(this::add);
    }


    public List<FragmentWrapper> getGatewayPlugins() {
        return gatewayPlugins;
    }

    public List<FragmentWrapper> getSmartLimiters() {
        return smartLimiters;
    }
}
