package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.processor.auth.AuthProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthProcessorTest extends BasePluginTest {

    @Autowired
    AuthProcessor authProcessor;

    @Test
    public void process() {

        String plugin = "{\"useAuthz\":true,\"kind\":\"jwt-auth\",\"authnType\":\"jwt_authn_type\","
                    + "\"cacheSwitch\":true,\"authz_result_cache\":{\"result_cache_key\":{\"ignore_case\":true},"
                    + "\"result_cache_ttl\":\"50000\"}}";

        FragmentHolder f = authProcessor.process(plugin, serviceInfo);
        Assert.assertEquals("need_authorization: true\n" +
                            "failure_auth_allow: false\n" +
                            "jwt_authn_type: {}\n" +
                            "authz_result_cache:\n" +
                            "  result_cache_ttl: 50000\n" +
                            "  result_cache_key:\n" +
                            "    ignore_case: true\n" +
                            "    headers_keys:\n" +
                            "    - authority", f.getVirtualServiceFragment().getContent().trim());
    }
}