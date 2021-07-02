package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.meta.ServiceInfo;
import org.hango.cloud.core.BaseTest;
import org.junit.Before;

public class BasePluginTest extends BaseTest {

    ServiceInfo serviceInfo = new ServiceInfo();

    @Before
    public void before() {
        serviceInfo.setApiName("api");
        serviceInfo.setHosts("hosts1");
        serviceInfo.setMethod("HTTP");
        serviceInfo.setPriority("100");
        serviceInfo.setSubset("sb1");
        serviceInfo.setServiceName("svvc");
        serviceInfo.setGateway("proxy");
        serviceInfo.setUri("/uri");
    }
}
