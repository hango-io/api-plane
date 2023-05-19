package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.ServiceInfo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("java:S1192")
public class ResponseBodyReWriteProcessorTest extends BasePluginTest {

    @Autowired
    private ResponseBodyReWriteProcessor processor;

    @Test
    public void process() {
        String p1 = "{\n" +
                "  \"request\":\n" +
                "  {\n" +
                "    \"requestSwitch\": true,\n" +
                "    \"method\":\n" +
                "    [\n" +
                "      \"GET\",\n" +
                "      \"POST\"\n" +
                "    ],\n" +
                "    \"path\":\n" +
                "    {\n" +
                "      \"match_type\": \"safe_regex_match\",\n" +
                "      \"value\": \"/a/*\"\n" +
                "    },\n" +
                "    \"host\":\n" +
                "    {\n" +
                "      \"match_type\": \"exact_match\",\n" +
                "      \"value\": \"www.163.com\"\n" +
                "    },\n" +
                "    \"headers\":\n" +
                "    [\n" +
                "      {\n" +
                "        \"headerKey\": \"Authorization\",\n" +
                "        \"match_type\": \"exact_match\",\n" +
                "        \"value\": \"qz\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"headerKey\": \"test\",\n" +
                "        \"match_type\": \"exact_match\",\n" +
                "        \"value\": \"testvalue\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"kind\": \"response-body-rewrite\",\n" +
                "  \"response\":\n" +
                "  {\n" +
                "    \"responseSwitch\": true,\n" +
                "    \"code\":\n" +
                "    {\n" +
                "      \"match_type\": \"exact_match\",\n" +
                "      \"value\": \"200\"\n" +
                "    },\n" +
                "    \"headers\":\n" +
                "    [\n" +
                "      {\n" +
                "        \"headerKey\": \"content-type\",\n" +
                "        \"match_type\": \"exact_match\",\n" +
                "        \"value\": \"application/json\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"bodyTransformList\":\n" +
                "  [\n" +
                "    {\n" +
                "      \"key\": \"/code\",\n" +
                "      \"value\": \"200\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"key\": \"/messgae\",\n" +
                "      \"value\": \"success\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        FragmentHolder f = processor.process(p1, new ServiceInfo());

        Assert.assertEquals("encoder_body_transformation:\n" +
                "  matcher:\n" +
                "    decoder_matcher:\n" +
                "      headers:\n" +
                "      - name: \":authority\"\n" +
                "        string_match:\n" +
                "          exact: \"www.163.com\"\n" +
                "      - name: \":method\"\n" +
                "        string_match:\n" +
                "          safe_regex:\n" +
                "            google_re2: {}\n" +
                "            regex: \"GET|POST\"\n" +
                "      - name: \":path\"\n" +
                "        string_match:\n" +
                "          safe_regex:\n" +
                "            google_re2: {}\n" +
                "            regex: \"/a/*\"\n" +
                "      - name: \"Authorization\"\n" +
                "        string_match:\n" +
                "          exact: \"qz\"\n" +
                "      - name: \"test\"\n" +
                "        string_match:\n" +
                "          exact: \"testvalue\"\n" +
                "    encoder_matcher:\n" +
                "      headers:\n" +
                "      - name: \"content-type\"\n" +
                "        string_match:\n" +
                "          exact: \"application/json\"\n" +
                "      - name: \":status\"\n" +
                "        string_match:\n" +
                "          safe_regex:\n" +
                "            google_re2: {}\n" +
                "            regex: \"200|\"\n" +
                "  json_body_transformation:\n" +
                "    json_transformations:\n" +
                "    - json_pointer: \"/code\"\n" +
                "      json_value: \"200\"\n" +
                "    - json_pointer: \"/messgae\"\n" +
                "      json_value: \"success\"", f.getVirtualServiceFragment().getContent().trim());
    }
}
