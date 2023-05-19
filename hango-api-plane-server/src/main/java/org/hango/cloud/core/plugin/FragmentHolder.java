package org.hango.cloud.core.plugin;

import java.util.ArrayList;
import java.util.List;

public class FragmentHolder {
    private FragmentWrapper virtualServiceFragment;

    // ratelimit插件的configmap
    private FragmentWrapper sharedConfigFragment;

    private FragmentWrapper gatewayPluginsFragment;

    // ratelimit插件的smartLimiter
    private List<FragmentWrapper> smartLimiterFragment = new ArrayList<>();


    public FragmentWrapper getVirtualServiceFragment() {
        return virtualServiceFragment;
    }

    public void setVirtualServiceFragment(FragmentWrapper virtualServiceFragment) {
        this.virtualServiceFragment = virtualServiceFragment;
    }

    public FragmentWrapper getSharedConfigFragment() {
        return sharedConfigFragment;
    }

    public void setSharedConfigFragment(FragmentWrapper sharedConfigFragment) {
        this.sharedConfigFragment = sharedConfigFragment;
    }

    public FragmentWrapper getGatewayPluginsFragment() {
        return gatewayPluginsFragment;
    }

    public void setGatewayPluginsFragment(FragmentWrapper gatewayPluginsFragment) {
        this.gatewayPluginsFragment = gatewayPluginsFragment;
    }

    public List<FragmentWrapper> getSmartLimiterFragment() {
        return smartLimiterFragment;
    }

    public void setSmartLimiterFragment(List<FragmentWrapper> smartLimiterFragment) {
        this.smartLimiterFragment = smartLimiterFragment;
    }
}
