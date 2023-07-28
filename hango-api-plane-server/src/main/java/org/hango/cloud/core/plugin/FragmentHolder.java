package org.hango.cloud.core.plugin;

import java.util.ArrayList;
import java.util.List;

public class FragmentHolder {

    private FragmentWrapper gatewayPluginsFragment;

    private List<FragmentWrapper> smartLimiterFragment = new ArrayList<>();


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
