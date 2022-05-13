package org.hango.cloud.util;

import org.hango.cloud.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.hango.cloud.meta.Service.ServiceLoadBalancer;
import org.hango.cloud.meta.Service.ServiceLoadBalancer.ConsistentHash;
import org.hango.cloud.meta.Service.ServiceLoadBalancer.ConsistentHash.HttpCookie;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
public class CommonUtilTest {



    @Test
    public void testValidIpPort() {

        String ip1 = "10.10.10.10:2131";
        String ip2 = "256.256.256.213:A";
        String ip3 = "10.10.10.10:65536";
        String ip4 = "10.10.10.10";

        Assert.assertTrue(CommonUtil.isValidIPPortAddr(ip1));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip2));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip3));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip4));
    }

    @Test
    public void testHost2Regex() {

        String h1 = "*.163.com";
        String h2 = "www.*.com";

        String add1 = "a.163.com";
        String add2 = ".163.com";
        String add3 = "www.163.com";
        String add4 = "www.com";

        Pattern p1 = Pattern.compile(CommonUtil.host2Regex(h1));
        Pattern p2 = Pattern.compile(CommonUtil.host2Regex(h2));

        Assert.assertTrue(p1.matcher(add1).find());
        Assert.assertTrue(!p1.matcher(add2).find());
        Assert.assertTrue(p1.matcher(add3).find());
        Assert.assertTrue(p2.matcher(add3).find());
        Assert.assertTrue(!p2.matcher(add4).find());

    }

    @Test
    public void testObj2Yaml() {

        ServiceLoadBalancer lb = new ServiceLoadBalancer();
        lb.setSimple("RANDOM");
        ConsistentHash consistentHash = new ConsistentHash();
        consistentHash.setHttpHeaderName("thisisheader");
        lb.setConsistentHash(consistentHash);

        HttpCookie cookie = new HttpCookie();
        cookie.setName("na");
        cookie.setPath("path");
        cookie.setTtl(30);

        consistentHash.setHttpCookie(cookie);

        Assert.assertNotNull(CommonUtil.obj2yaml(lb));
    }

    @Test
    public void testYaml2Obj() {

        String vsYaml = "kind: VirtualService\n" +
                "metadata:\n" +
                "  creationTimestamp: 2019-08-20T12:41:10Z\n" +
                "  generation: 1\n" +
                "  labels:\n" +
                "    api_service: service-zero\n" +
                "  name: service-zero-gateway-yx\n" +
                "  namespace: gateway-system\n" +
                "  resourceVersion: \"30048280\"\n" +
                "  selfLink: /apis/networking.istio.io/v1alpha3/namespaces/gateway-system/virtualservices/service-zero-gateway-yx\n" +
                "  uid: c98eacf7-c347-11e9-8a87-fa163e5fcbdd\n" +
                "spec:\n" +
                "  gateways:\n" +
                "  - service-zero-gateway-yx\n" +
                "  hosts:\n" +
                "  - www.test.com\n" +
                "  http:\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        plugin:\n" +
                "          regex: rewrite\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      queryParams:\n" +
                "        plugin:\n" +
                "          regex: rewrite\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    requestTransform:\n" +
                "      new:\n" +
                "        path: /{{backendUrl}}\n" +
                "      original:\n" +
                "        path: /rewrite/{backendUrl}\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        Cookie:\n" +
                "          regex: .*(?:;|^)plugin=r.*(?:;|$).*\n" +
                "        plugin:\n" +
                "          regex: redirect\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    redirect:\n" +
                "      uri: /redirect\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        plugin:\n" +
                "          regex: return\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    return:\n" +
                "      body:\n" +
                "        inlineString: '{is return plugin}'\n" +
                "      code: 403\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100\n" +
                "  - match:\n" +
                "    - method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 30\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 70\n" +
                "  - match:\n" +
                "    - method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: .*\n" +
                "    api: plane-istio-test-1\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100";

        VirtualService virtualService = CommonUtil.yaml2Obj(vsYaml, VirtualService.class);
        Assert.assertNotNull(virtualService);
    }


    @Test
    public void testMergeList() {

        List<String> oldL = Arrays.asList("a", "b", "b", "c", "d");
        List<String> newL = Arrays.asList("b", "c", "c", "e", "f");

        /**
         * excepted a, b, c, c, d, e, f
         */

        List mergedList = CommonUtil.mergeList(oldL, newL, (Equals<String>) (ot, nt) -> ot.equals(nt));

        Assert.assertEquals(7, mergedList.size());
        Assert.assertEquals(1, getCount(mergedList, "b"));
        Assert.assertEquals(2, getCount(mergedList, "c"));
        Assert.assertEquals(1, getCount(mergedList, "d"));
        Assert.assertEquals(1, getCount(mergedList, "e"));
    }

    @Test
    public void testDropList() {

        List<String> oldL = Arrays.asList("a", "b", "b", "c", "d", "d");

        List droppedList = CommonUtil.dropList(oldL, "b", (Equals<String>) (ot, nt) -> ot.equals(nt));
        List droppedList1 = CommonUtil.dropList(droppedList, "d", (Equals<String>) (ot, nt) -> ot.equals(nt));

        Assert.assertEquals(4, droppedList.size());
        Assert.assertTrue(droppedList.containsAll(Arrays.asList("a", "c", "d", "d")));

        Assert.assertEquals(2, droppedList1.size());
        Assert.assertTrue(droppedList.containsAll(Arrays.asList("a", "c")));
    }

    private static int getCount(List<String> list, String str) {

        int count = 0;
        for (String s : list) {
            if (str.equals(s)) count++;
        }
        return count;
    }

    @Test
    public void testXIndexOf() {

        String str = "outbound|9901|subset1|istio-galley.istio-system.svc.cluster.local";
        String occur = "|";

        Assert.assertEquals(-1, CommonUtil.xIndexOf("", occur, 0));
        Assert.assertEquals(-1, CommonUtil.xIndexOf(str, "", 0));
        Assert.assertEquals(-1, CommonUtil.xIndexOf(str, occur, -1));
        Assert.assertEquals(8, CommonUtil.xIndexOf(str, occur, 0));
        Assert.assertEquals(13, CommonUtil.xIndexOf(str, occur, 1));
        Assert.assertEquals(21, CommonUtil.xIndexOf(str, occur, 2));
        Assert.assertEquals(-1, CommonUtil.xIndexOf(str, occur, 3));

    }
}
