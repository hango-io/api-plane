package org.hango.cloud.core.plugin;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/15
 **/
public class FragmentHolder {
    private FragmentWrapper virtualServiceFragment;

    // ratelimit插件的configmap
    private FragmentWrapper sharedConfigFragment;

    private FragmentWrapper gatewayPluginsFragment;

    // ratelimit插件的smartLimiter
    private FragmentWrapper smartLimiterFragment;


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

    public FragmentWrapper getSmartLimiterFragment() {
        return smartLimiterFragment;
    }

    public void setSmartLimiterFragment(FragmentWrapper smartLimiterFragment) {
        this.smartLimiterFragment = smartLimiterFragment;
    }
}
