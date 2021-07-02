package org.hango.cloud.core.gateway;

import org.hango.cloud.core.BaseTest;
import org.hango.cloud.meta.API;
import org.hango.cloud.meta.Service;
import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.meta.UriMatch;
import org.junit.Before;

import java.util.Arrays;

public class BaseGatewayTest extends BaseTest {

	API api = new API();
	Service service = new Service();

	@Before
	public void before() {
		service.setCode("dynamic-1");
		service.setBackendService("istio-e2e.apigw-demo.svc.cluster.local");
		service.setType("DYNAMIC");
		service.setWeight(100);
		service.setPort(80);
		service.setGateway("demo");

		api.setName("123");
		api.setHosts(Arrays.asList("istio.com"));
		api.setGateways(Arrays.asList("demo"));
		api.setRequestUris(Arrays.asList("/e2e"));
		api.setUriMatch(UriMatch.exact);
		api.setMethods(Arrays.asList("*"));
		api.setProxyServices(Arrays.asList(service));
		api.setPlugins(Arrays.asList("{\"limit_by_list\":[{\"headers\":[],\"day\":\"\",\"second\":55}],"
		                             + "\"kind\":\"local-limiting\",\"name\":\"local-limiting\"}"));
		api.setProtocol("HTTP");
		api.setPort(80);
		api.setPriority(100);
		api.setServiceTag("e2e");
		api.setApiId(Long.valueOf(123));
		api.setApiName("e2eunit");
		api.setProjectId("3");
	}

}
