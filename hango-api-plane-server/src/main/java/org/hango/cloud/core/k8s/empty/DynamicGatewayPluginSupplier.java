package org.hango.cloud.core.k8s.empty;

import org.hango.cloud.k8s.K8sTypes;

import java.util.function.Supplier;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/23
 **/
public class DynamicGatewayPluginSupplier implements Supplier<K8sTypes.EnvoyPlugin> {

    private String gw;
    private String name;
    private String format;

    public DynamicGatewayPluginSupplier(String gw, String name, String format) {
        this.gw = gw;
        this.name = name;
        this.format = format;
    }

    @Override
    public K8sTypes.EnvoyPlugin get() {
        String realName = String.format(format, name, gw);
        return new EmptyGatewayPlugin(realName);
    }
}
