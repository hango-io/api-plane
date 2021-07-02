package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.IstioGateway;

import java.util.Arrays;
import java.util.List;

public class PortalGatewayDataHandler extends GatewayDataHandler {

    private Boolean enableHttp10;

    public PortalGatewayDataHandler(Boolean enableHttp10) {
        this.enableHttp10 = enableHttp10;
    }
    @Override
    List<TemplateParams> doHandle(TemplateParams tp, IstioGateway istioGateway) {
        TemplateParams params = TemplateParams.instance()
                .put(TemplateConst.GATEWAY_NAME, istioGateway.getName())
                .put(TemplateConst.GATEWAY_HTTP_10, enableHttp10)
                .put(TemplateConst.GATEWAY_GW_CLUSTER, istioGateway.getGwCluster());
        return Arrays.asList(params);
    }
}
