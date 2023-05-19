package org.hango.cloud.core.k8s.subtracter;

import org.hango.cloud.core.editor.PathExpressionEnum;

public class GatewayRateLimitConfigMapSubtracter extends RateLimitConfigMapSubtracter {

    private String gateway;
    private String api;

    public GatewayRateLimitConfigMapSubtracter(String gateway, String api) {
        this.gateway = gateway;
        this.api = api;
    }

    @Override
    public String getPath() {
        return PathExpressionEnum.REMOVE_GATEWAY_RATELIMIT_CONFIGMAP_BY_VALUE.translate(gateway, api);
    }
}
