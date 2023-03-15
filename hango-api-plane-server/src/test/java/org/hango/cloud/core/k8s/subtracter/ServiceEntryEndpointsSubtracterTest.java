package org.hango.cloud.core.k8s.subtracter;

import com.google.common.collect.ImmutableMap;
import org.hango.cloud.util.CommonUtil;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;
import org.junit.Assert;
import org.junit.Test;

public class ServiceEntryEndpointsSubtracterTest {

    @Test
    public void subtract() {

        ServiceEntryEndpointsSubtracter subtracter = new ServiceEntryEndpointsSubtracter("gw1");

        String yaml = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: ServiceEntry\n" +
                "metadata:\n" +
                "  name: static-799\n" +
                "  namespace: gateway-system\n" +
                "spec:\n" +
                "  endpoints:\n" +
                "  - address: 127.0.0.1\n" +
                "    labels:\n" +
                "      gw_cluster: gw1\n" +
                "    ports:\n" +
                "      http: 9999\n" +
                "  - address: 127.0.0.1\n" +
                "    labels:\n" +
                "      gw_cluster: gw2\n" +
                "    ports:\n" +
                "      http: 9999\n" +
                "  exportTo:\n" +
                "  - '*'\n" +
                "  hosts:\n" +
                "  - com.netease.static-799\n" +
                "  location: MESH_EXTERNAL\n" +
                "  ports:\n" +
                "  - name: http\n" +
                "    number: 80\n" +
                "    protocol: HTTP\n" +
                "  resolution: DNS\n";

        ServiceEntry serviceEntry = CommonUtil.yaml2Obj(yaml, ServiceEntry.class);

        ServiceEntry subtractedSe = subtracter.subtract(serviceEntry);

        Assert.assertEquals(1, subtractedSe.getSpec().getEndpoints().size());
        Assert.assertEquals(ImmutableMap.of("gw_cluster", "gw2"), subtractedSe.getSpec().getEndpoints().get(0).getLabels());
    }
}