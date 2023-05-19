package org.hango.cloud.core.k8s.subtracter;

import me.snowdrop.istio.api.networking.v1alpha3.HTTPMatchRequest;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceSpec;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GatewayVirtualServiceSubtracterTest {

    @Test
    public void subtract() {

        GatewayVirtualServiceSubtracter subtracter1 = new GatewayVirtualServiceSubtracter("a");
        GatewayVirtualServiceSubtracter subtracter2 = new GatewayVirtualServiceSubtracter("b");

        VirtualService old = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                        getHTTPRoute("a", null),
                        getHTTPRoute("b",
                                Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                        getHTTPRoute("a", null),
                        getHTTPRoute("c", null))
        ));

//        VirtualService result = subtracter1.subtract(old);
//        Assert.assertTrue(result.getSpec().getHttp().size() == 2);
//        result = subtracter2.subtract(old);
//        Assert.assertTrue(result.getSpec().getHttp().size() == 1);
    }


    private static HTTPRoute getHTTPRoute(String api, List<HTTPMatchRequest> requests) {
        HTTPRoute route = new HTTPRoute();
        route.setName(api);
        route.setMatch(requests);
        return route;
    }

    private static HTTPMatchRequest getHTTPMatchRequest() {
        return new HTTPMatchRequest();
    }

    private static VirtualServiceSpec getVirtualServiceSpec(List<HTTPRoute> routes) {
        VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHttp(routes);
        return spec;
    }

    private static VirtualService getVirtualService(VirtualServiceSpec spec) {
        VirtualService vs = new VirtualService();
        vs.setSpec(spec);
        return vs;
    }
}
