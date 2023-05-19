package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.ServiceInfo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("java:S1192")
public class RequestBodyReWriteProcessorTest extends BasePluginTest {

    @Autowired
    private RequestBodyReWriteProcessor processor;

    @Test
    public void process() {
        String p1 = "{\n" +
                "  \"request\":\n" +
                "  {\n" +
                "    \"requestSwitch\": true,\n" +
                "    \"method\":\n" +
                "    [\n" +
                "      \"POST\"\n" +
                "    ],\n" +
                "    \"path\":\n" +
                "    {\n" +
                "      \"match_type\": \"exact_match\",\n" +
                "      \"value\": \"/testbody\"\n" +
                "    },\n" +
                "    \"host\":\n" +
                "    {\n" +
                "      \"match_type\": \"safe_regex_match\",\n" +
                "      \"value\": \"*\"\n" +
                "    },\n" +
                "    \"headers\":\n" +
                "    []\n" +
                "  },\n" +
                "  \"kind\": \"request-body-rewrite\",\n" +
                "  \"bodyTransformList\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"key\": \"/a\",\n" +
                "      \"value\": \"2\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        FragmentHolder f = processor.process(p1, new ServiceInfo());

        Assert.assertEquals("decoder_body_transformation:\n" +
                "  matcher:\n" +
                "    decoder_matcher:\n" +
                "      headers:\n" +
                "      - name: \":authority\"\n" +
                "        string_match:\n" +
                "          safe_regex:\n" +
                "            google_re2: {}\n" +
                "            regex: \"*\"\n" +
                "      - name: \":method\"\n" +
                "        string_match:\n" +
                "          exact: \"POST\"\n" +
                "      - name: \":path\"\n" +
                "        string_match:\n" +
                "          exact: \"/testbody\"\n" +
                "  json_body_transformation:\n" +
                "    json_transformations:\n" +
                "    - json_pointer: \"/a\"\n" +
                "      json_value: \"2\"", f.getVirtualServiceFragment().getContent().trim());
    }
}
