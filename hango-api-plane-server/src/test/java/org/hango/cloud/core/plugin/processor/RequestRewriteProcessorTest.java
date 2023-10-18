package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author zhufengwei
 * @Date 2023/8/17
 */
@SuppressWarnings("java:S1192")
public class RequestRewriteProcessorTest extends BasePluginTest {
    @Autowired
    RequestRewriterProcessor requestRewriterProcessor;

    @Test
    public void process() {

        String p1 = "{\n" +
                "  \"headers\": [\n" +
                "    {\n" +
                "      \"key\": \"x-test-header\",\n" +
                "      \"action\": \"update\",\n" +
                "      \"text\": \"header_value\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"kind\": \"transformer\",\n" +
                "  \"querystrings\": [\n" +
                "    {\n" +
                "      \"key\": \"queryKey\",\n" +
                "      \"action\": \"remove\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";


        FragmentHolder process = requestRewriterProcessor.process(p1, serviceInfo);

        Assert.assertEquals("config:\n" +
                "  decoder_rewriters:\n" +
                "    rewriters:\n" +
                "    - update: \"header_value\"\n" +
                "      header_name: \"x-test-header\"\n" +
                "    - remove: {}\n" +
                "      parameter: \"queryKey\"\n", process.getGatewayPluginsFragment().getContent());
    }
}
