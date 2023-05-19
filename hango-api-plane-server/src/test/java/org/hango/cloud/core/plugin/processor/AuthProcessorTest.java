package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.core.plugin.processor.auth.SimpleAuthProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("java:S106")
public class AuthProcessorTest extends BasePluginTest {

    @Autowired
    SimpleAuthProcessor simpleAuthProcessor;

    @Test
    public void process() {
        testSimpleAuth();
    }

    private void testSimpleAuth() {
        String plugin = "{\"kind\":\"simple-auth\",\"authnType\":\"simple_authn_type\"," +
                "\"appNameSetting\":{\"parameterType\":\"header\",\"parameterName\":\"x-auth\"}}";

        FragmentHolder f = simpleAuthProcessor.process(plugin, serviceInfo);
        System.out.println(f.getVirtualServiceFragment().getContent().trim());
        Assert.assertEquals("need_authorization: false\n" +
                "missing_auth_allow: false\n" +
                "authn_policy_name: proxy.super_authz.authn_policy.token_authn_policy\n" +
                "authn_policy_config:\n" +
                "  token_format: ANY\n" +
                "  token_source: x-auth\n" +
                "  token_rename: authorization\n" +
                "  send_context: true", f.getVirtualServiceFragment().getContent().trim());
    }
}