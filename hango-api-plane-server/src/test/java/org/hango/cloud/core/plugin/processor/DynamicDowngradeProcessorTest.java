package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DynamicDowngradeProcessorTest extends BasePluginTest {

    @Autowired
    DynamicDowngradeProcessor dynamicDowngradeProcessor;

    @Test
    public void process() {

        String p1 = "{\n" +
                "\t\"condition\": {\n" +
                "\t\t\"request\": {\n" +
                "\t\t\t\"requestSwitch\": true,\n" +
                "\t\t\t\"path\": {\n" +
                "\t\t\t\t\"match_type\": \"safe_regex_match\",\n" +
                "\t\t\t\t\"value\": \"/anything/anythin.\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"host\": {\n" +
                "\t\t\t\t\"match_type\": \"safe_regex_match\",\n" +
                "\t\t\t\t\"value\": \"103.196.65.17.\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"headers\": [{\n" +
                "\t\t\t\t\"headerKey\": \"key\",\n" +
                "\t\t\t\t\"match_type\": \"exact_match\",\n" +
                "\t\t\t\t\"value\": \"va\"\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"method\": [\n" +
                "\t\t\t\t\"GET\"\n" +
                "\t\t\t]\n" +
                "\t\t},\n" +
                "\t\t\"response\": {\n" +
                "\t\t\t\"code\": {\n" +
                "\t\t\t\t\"match_type\": \"exact_match\",\n" +
                "\t\t\t\t\"value\": \"200\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"headers\": []\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"kind\": \"dynamic-downgrade\",\n" +
                "\t\"cache\": {\n" +
                "\t\t\"condition\": {\n" +
                "\t\t\t\"response\": {\n" +
                "\t\t\t\t\"code\": {\n" +
                "\t\t\t\t\t\"match_type\": \"safe_regex_match\",\n" +
                "\t\t\t\t\t\"value\": \"2..\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"headers\": [{\n" +
                "\t\t\t\t\t\"headerKey\": \"x-can-downgrade\",\n" +
                "\t\t\t\t\t\"match_type\": \"exact_match\",\n" +
                "\t\t\t\t\t\"value\": \"true\"\n" +
                "\t\t\t\t}]\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"ttls\": {\n" +
                "\t\t\t\"default\": 30000,\n" +
                "\t\t\t\"custom\": [{\n" +
                "\t\t\t\t\"code\": \"200\",\n" +
                "\t\t\t\t\"ttl\": 50000\n" +
                "\t\t\t}]\n" +
                "\t\t},\n" +
                "\t\t\"cache_key\": {\n" +
                "\t\t\t\"query_params\": [\"id\"],\n" +
                "\t\t\t\"headers\": [\"comefrom\"]\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        String p2 = "{\n" +
                "  \"condition\": {\n" +
                "    \"request\": {\n" +
                "      \"requestSwitch\": true,\n" +
                "      \"path\": {\n" +
                "        \"match_type\": \"safe_regex_match\",\n" +
                "        \"value\": \"/anything/anythin.\"\n" +
                "      },\n" +
                "      \"host\": {\n" +
                "        \"match_type\": \"safe_regex_match\",\n" +
                "        \"value\": \"103.196.65.17.\"\n" +
                "      },\n" +
                "      \"headers\": [\n" +
                "        {\n" +
                "          \"headerKey\": \"key\",\n" +
                "          \"match_type\": \"exact_match\",\n" +
                "          \"value\": \"va\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"method\": [\n" +
                "        \"GET\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"response\": {\n" +
                "      \"code\": {\n" +
                "        \"match_type\": \"exact_match\",\n" +
                "        \"value\": \"200\"\n" +
                "      },\n" +
                "      \"headers\": []\n" +
                "    }\n" +
                "  },\n" +
                "  \"kind\": \"dynamic-downgrade\",\n" +
                "  \"httpx\":{\n" +
                "    \"uri\":\"http://httpbin.org/anything\"\n" +
                "  }\n" +
                "}";

        String p3 = "{\n" +
                "  \"cache\": {\n" +
                "    \"cache_key\": {},\n" +
                "    \"condition\": {\n" +
                "      \"response\": {\n" +
                "        \"headers\": [\n" +
                "          {\n" +
                "            \"match_type\": \"exact_match\",\n" +
                "            \"headerKey\": \"server\",\n" +
                "            \"value\": \"envoy\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"code\": {\n" +
                "          \"match_type\": \"safe_regex_match\",\n" +
                "          \"value\": \"200\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"local\": false,\n" +
                "    \"ttl\": {\n" +
                "      \"default\": 30000,\n" +
                "      \"custom\": [\n" +
                "        {\n" +
                "          \"code\": \"200\",\n" +
                "          \"ttl\": 50000\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"condition\": {\n" +
                "    \"response\": {\n" +
                "      \"code\": {\n" +
                "        \"match_type\": \"safe_regex_match\",\n" +
                "        \"value\": \"500\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"config\": {},\n" +
                "  \"httpx\": {},\n" +
                "  \"kind\": \"dynamic-downgrade\",\n" +
                "  \"response\": {}\n" +
                "}";

        FragmentHolder f1 = dynamicDowngradeProcessor.process(p1, serviceInfo);
        FragmentHolder f2 = dynamicDowngradeProcessor.process(p2, serviceInfo);
        FragmentHolder f3 = dynamicDowngradeProcessor.process(p3, serviceInfo);
        Assert.assertEquals("downgrade_rpx:\n"
            + "  headers:\n"
            + "  - name: \":status\"\n"
            + "    string_match:\n"
            + "      safe_regex:\n"
            + "        google_re2: {}\n"
            + "        regex: \"500|\"\n"
            + "cache_rpx_rpx:\n"
            + "  headers:\n"
            + "  - name: \":status\"\n"
            + "    string_match:\n"
            + "      safe_regex:\n"
            + "        google_re2: {}\n"
            + "        regex: \"200|\"\n"
            + "  - name: \"server\"\n"
            + "    string_match:\n"
            + "      exact: \"envoy\"\n"
            + "cache_ttls:\n"
            + "  RedisHttpCache:\n"
            + "    default: 30000\n"
            + "    customs:\n"
            + "      \"200\": 50000\n"
            + "  LocalHttpCache:\n"
            + "    default: 30000\n"
            + "    customs:\n"
            + "      \"200\": 50000\n"
            + "key_maker:\n"
            + "  query_params: []\n"
            + "  headers_keys: []", f3.getVirtualServiceFragment().getContent().trim());
    }
}