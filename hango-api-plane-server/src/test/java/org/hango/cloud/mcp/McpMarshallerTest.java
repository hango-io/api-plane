package org.hango.cloud.mcp;

import istio.mcp.nsf.SnapshotOuterClass;
import istio.mcp.v1alpha1.ResourceOuterClass;
import istio.networking.v1alpha3.*;
import org.junit.Test;
import slime.microservice.plugin.v1alpha1.PluginManagerOuterClass;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/6
 **/
public class McpMarshallerTest {

    @Test
    public void testMarshaller() {
        McpOptions options = new McpOptions();
        options.registerDescriptor(SnapshotOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(ResourceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(VirtualServiceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(DestinationRuleOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(PluginManagerOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayPluginOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(ServiceEntryOuterClass.getDescriptor().getMessageTypes());

        McpMarshaller marshaller = new McpMarshaller(options);

        String data = "{\"gateways\":[\"gateway-proxy-2\"],\"hosts\":[\"httpbin.com\"],\"http\":[{\"api\":\"3333\",\"match\":[{\"uri\":{\"regex\":\"(.*)\"}}],\"meta\":{\"qz_cluster_name\":\"gateway-proxy-2\",\"qz_svc_id\":\"httpbin\",\"qz_api_id\":3333,\"qz_tenant_id\":3333,\"qz_project_id\":3333,\"qz_api_name\":\"testApiplane\"},\"priority\":82,\"route\":[{\"destination\":{\"host\":\"httpbin.service.consul.consul1\",\"port\":{\"number\":80},\"subset\":\"dynamic-3333-gateway-proxy-2\"},\"weight\":100}],\"timeout\":\"60000ms\"}],\"priority\":100400782}";

        VirtualServiceOuterClass.VirtualService.Builder builder = VirtualServiceOuterClass.VirtualService.newBuilder();

        marshaller.merge(data, builder);
        VirtualServiceOuterClass.VirtualService vs = builder.build();
    }
}
