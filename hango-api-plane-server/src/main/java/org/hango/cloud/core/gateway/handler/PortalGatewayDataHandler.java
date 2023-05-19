package org.hango.cloud.core.gateway.handler;

import org.hango.cloud.core.template.TemplateConst;
import org.hango.cloud.core.template.TemplateParams;
import org.hango.cloud.meta.IstioGateway;
import org.hango.cloud.meta.IstioGatewayServer;
import org.hango.cloud.meta.IstioGatewayTLS;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

public class PortalGatewayDataHandler extends GatewayDataHandler {

    private Boolean enableHttp10;
    private String gatewayNamespace;
    public static final String CREDENTIAL_NAME_PREFIX = "kubernetes-gateway://";

    public PortalGatewayDataHandler(Boolean enableHttp10, String gatewayNamespace) {
        this.enableHttp10 = enableHttp10;
        this.gatewayNamespace = gatewayNamespace;
    }
    @Override
    List<TemplateParams> doHandle(TemplateParams tp, IstioGateway istioGateway) {
        List<IstioGatewayServer> servers = istioGateway.getServers();
        if (!CollectionUtils.isEmpty(servers)){
            for (IstioGatewayServer server : servers) {
                IstioGatewayTLS istioGatewayTLS = server.getIstioGatewayTLS();
                if (istioGatewayTLS != null){
                    String credentialName = CREDENTIAL_NAME_PREFIX + gatewayNamespace + "/" + istioGatewayTLS.getCredentialName();
                    istioGatewayTLS.setCredentialName(credentialName);
                }
            }
        }
        TemplateParams params = TemplateParams.instance()
                .put(TemplateConst.GATEWAY_NAME, istioGateway.getName())
                .put(TemplateConst.GATEWAY_HTTP_10, enableHttp10)
                .put(TemplateConst.GATEWAY_GW_CLUSTER, istioGateway.getGwCluster())
                .put(TemplateConst.GATEWAY_SERVERS, istioGateway.getServers());
        return Arrays.asList(params);
    }
}
