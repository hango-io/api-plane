package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class LocalCacheProcessorTest extends BasePluginTest {

    @Autowired
    LocalCacheProcessor cacheProcessor;

    @Test
    public void process() {

        String p1 = "{\n" + "    \"condition\":{\n" + "        \"request\":{\n"
                    + "            \"requestSwitch\":true,\n" + "            \"path\":{\n"
                    + "                \"match_type\":\"exact_match\",\n" + "                \"value\":\"/abc\"\n"
                    + "            },\n" + "            \"host\":{\n"
                    + "                \"match_type\":\"exact_match\",\n" + "                \"value\":\"abc.com\"\n"
                    + "            },\n" + "            \"headers\":[\n" + "\n" + "            ],\n"
                    + "            \"method\":[\n" + "                \"GET\"\n" + "            ]\n" + "        },\n"
                    + "        \"response\":{\n" + "            \"responseSwitch\":true,\n" + "            \"code\":{\n"
                    + "                \"match_type\":\"exact_match\",\n" + "                \"value\":\"200\"\n"
                    + "            },\n" + "            \"headers\":[\n" + "\n" + "            ]\n" + "        }\n"
                    + "    },\n" + "    \"ttl\":{\n" + "        \"local\":{\n" + "            \"custom\":[\n" + "\n"
                    + "            ],\n" + "            \"default\":\"2000\"\n" + "        },\n"
                    + "        \"redis\":{\n" + "            \"custom\":[\n" + "\n" + "            ],\n"
                    + "            \"default\":\"0\"\n" + "        }\n" + "    },\n" + "    \"kind\":\"cache\",\n"
                    + "    \"keyMaker\":{\n" + "        \"excludeHost\":true,\n" + "        \"ignoreCase\":true,\n"
                    + "        \"queryString\":[\n" + "            \"query1\"\n" + "        ],\n"
                    + "        \"headers\":[\n" + "            \"header1\"\n" + "        ]\n" + "    }\n" + "}";

        FragmentHolder f = cacheProcessor.process(p1, serviceInfo);
        Assert.assertEquals( "key_maker:\n"
            + "  exclude_host: true\n"
            + "  ignore_case: true\n"
            + "  headers_keys:\n"
            + "  - \"header1\"\n"
            + "  query_params:\n"
            + "  - \"query1\"\n"
            + "enable_rqx:\n"
            + "  headers:\n"
            + "  - name: \":authority\"\n"
            + "    string_match:\n"
            + "      exact: \"abc.com\"\n"
            + "  - name: \":method\"\n"
            + "    string_match:\n"
            + "      exact: \"GET\"\n"
            + "  - name: \":path\"\n"
            + "    string_match:\n"
            + "      exact: \"/abc\"\n"
            + "enable_rpx:\n"
            + "  headers:\n"
            + "  - name: \":status\"\n"
            + "    string_match:\n"
            + "      safe_regex:\n"
            + "        google_re2: {}\n"
            + "        regex: \"200|\"\n"
            + "cache_ttls:\n"
            + "  LocalHttpCache:\n"
            + "    default: 2000", f.getVirtualServiceFragment().getContent().trim());
    }
}