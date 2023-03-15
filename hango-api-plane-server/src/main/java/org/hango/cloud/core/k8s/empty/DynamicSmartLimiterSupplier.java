package org.hango.cloud.core.k8s.empty;

import org.hango.cloud.k8s.K8sTypes;

import java.util.function.Supplier;

public class DynamicSmartLimiterSupplier implements Supplier<K8sTypes.SmartLimiter> {

    private String gw;
    private String name;
    private String format;

    public DynamicSmartLimiterSupplier(String gw, String name, String format) {
        this.gw = gw;
        this.name = name;
        this.format = format;
    }

    @Override
    public K8sTypes.SmartLimiter get() {
        String realName = String.format(format, name, gw);
        return new EmptySmartLimiter(realName);
    }
}
