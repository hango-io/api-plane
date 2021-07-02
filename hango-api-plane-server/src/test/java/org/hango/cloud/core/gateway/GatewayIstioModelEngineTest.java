package org.hango.cloud.core.gateway;

import static org.junit.Assert.*;

import org.hango.cloud.core.BaseTest;
import org.hango.cloud.core.k8s.K8sResourcePack;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GatewayIstioModelEngineTest extends BaseGatewayTest {

	@Autowired
	GatewayIstioModelEngine gatewayIstioModelEngine;

	@Test
	public void translateApi() {
		List<K8sResourcePack> resourcePacks = gatewayIstioModelEngine.translate(api, false);
		Assert.assertTrue(resourcePacks.size() == 2);
		Assert.assertEquals(resourcePacks.get(0).getResource().getKind(), "VirtualService");
		Assert.assertEquals(resourcePacks.get(1).getResource().getKind(), "EnvoyPlugin");
	}

	@Test
	public void translateService() {
		List<K8sResourcePack> resourcePacks = gatewayIstioModelEngine.translate(service);
		Assert.assertTrue(resourcePacks.size() == 1);
		Assert.assertEquals(resourcePacks.get(0).getResource().getKind(), "DestinationRule");
	}

	@Test
	public void translate3() {
	}

	@Test
	public void translate4() {
	}
}