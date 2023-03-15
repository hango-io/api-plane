package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.util.CommonUtil;
import me.snowdrop.istio.api.networking.v1alpha3.Sidecar;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@SuppressWarnings("java:S1192")
public class SidecarOperatorTest {

    @Test
    public void testMerge() {

        String yaml1 = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: Sidecar\n" +
                "metadata:\n" +
                "  name: a\n" +
                "  namespace: powerful-debug-2\n" +
                "spec:\n" +
                "  workloadSelector:\n" +
                "    labels:\n" +
                "      app: a\n" +
                "  egress:\n" +
                "  - bind: 0.0.0.0\n" +
                "    hosts:\n" +
                "    - \"*/b.powerful-debug-2.svc.cluster.local\"\n" +
                "    - \"*/c.powerful-debug-2.svc.cluster.local\"\n";

        String yaml2 = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: Sidecar\n" +
                "metadata:\n" +
                "  name: a\n" +
                "  namespace: powerful-debug-2\n" +
                "spec:\n" +
                "  workloadSelector:\n" +
                "    labels:\n" +
                "      app: a\n" +
                "  egress:\n" +
                "  - bind: 0.0.0.0\n" +
                "    hosts:\n" +
                "    - \"*/b.powerful-debug-2.svc.cluster.local\"\n" +
                "    - \"*/c.powerful-debug-2.svc.cluster.local\"\n" +
                "    - \"*/d.powerful-debug-2.svc.cluster.local\"\n";

        String yaml3 = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: Sidecar\n" +
                "metadata:\n" +
                "  name: a\n" +
                "  namespace: powerful-debug-2\n" +
                "spec:\n" +
                "  workloadSelector:\n" +
                "    labels:\n" +
                "      app: a\n" +
                "  egress:\n" +
                "  - bind: 0.0.0.0\n" +
                "    hosts:\n" +
                "    - \"*/b.powerful-debug-2.svc.cluster.local\"\n";


        Sidecar sidecar1 = CommonUtil.yaml2Obj(yaml1, Sidecar.class);
        Sidecar sidecar2 = CommonUtil.yaml2Obj(yaml2, Sidecar.class);

        Sidecar sidecar1_1 = CommonUtil.yaml2Obj(yaml1, Sidecar.class);
        Sidecar sidecar3 = CommonUtil.yaml2Obj(yaml3, Sidecar.class);

        SidecarOperator operator = new SidecarOperator();

        Sidecar merged1 = operator.merge(sidecar1, null);
        Assert.assertEquals(sidecar1, merged1);
        Sidecar merged2 = operator.merge(null, sidecar2);
        Assert.assertEquals(sidecar2, merged2);
        Sidecar merged3 = operator.merge(sidecar1, sidecar2);
        Assert.assertEquals(3, getHosts(merged3).size());
        Sidecar merged4 = operator.merge(sidecar1_1, sidecar3);
        Assert.assertEquals(2, getHosts(merged4).size());

    }

    private List<String> getHosts(Sidecar sidecar) {
        return sidecar.getSpec().getEgress().get(0).getHosts();
    }

}
