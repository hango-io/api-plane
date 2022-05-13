package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RateLimitProcessorTest extends BasePluginTest {

    @Autowired
    RateLimitProcessor processor;

    @Test
    public void process() {

        String plugin1 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"identifier_extractor\": \"Header[plugin]\",\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"operator\": \"=\",\n" +
                "      \"right_value\": \"ratelimit\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"second\": 5,\n" +
                "    \"hour\": 10\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String plugin2 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"identifier_extractor\": \"Header[plugin]\",\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"operator\": \"present\",\n" +
                "      \"invert\": true\n" +
                "    }\n" +
                "    ],\n" +
                "    \"hour\": 1\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String plugin3 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin1]\",\n" +
                "      \"operator\": \"present\",\n" +
                "      \"invert\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin2]\",\n" +
                "      \"operator\": \"=\",\n" +
                "      \"right_value\": \"ratelimit\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"hour\": 1\n" +
                "  }\n" +
                "  ]\n" +
                "}";
        String plugin4 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin1]\",\n" +
                "      \"operator\": \"present\",\n" +
                "      \"invert\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin2]\",\n" +
                "      \"operator\": \"=\",\n" +
                "      \"right_value\": \"ratelimit\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"hour\": 1,\n" +
                "    \"type\": \"Local\"\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String plugin5 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "    {\n" +
                "      \"hour\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String plugin6 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"identifier_extractor\": \"Header[plugin]\",\n" +
                "    \"second\": 5,\n" +
                "    \"hour\": 10\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String plugin7 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"identifier_extractor\": \"Header[plugin]\",\n" +
                "    \"second\": 5,\n" +
                "    \"hour\": \"\"\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        FragmentHolder fragment1 = processor.process(plugin1, serviceInfo);
        FragmentHolder fragment2 = processor.process(plugin2, serviceInfo);
        FragmentHolder fragment3 = processor.process(plugin3, serviceInfo);
        FragmentHolder fragment4 = processor.process(plugin4, serviceInfo);
        FragmentHolder fragment5 = processor.process(plugin4, nullInfo);
        FragmentHolder fragment6 = processor.process(plugin5, serviceInfo);
        FragmentHolder fragment7 = processor.process(plugin6, serviceInfo);
        FragmentHolder fragment8 = processor.process(plugin7, serviceInfo);
        //TODO assert

        Assert.assertEquals("domain: \"qingzhou\"\n" +
                "descriptors:\n" +
                "- key: \"generic_key\"\n" +
                "  value: \"Service[svvc]-User[none]-Gateway[proxy]-Api[api]-Id[hash:-1360152201]\"\n" +
                "  descriptors:\n" +
                "  - key: \"WithoutValueHeader[plugin]\"\n" +
                "    rate_limit:\n" +
                "      unit: \"SECOND\"\n" +
                "      requests_per_unit: 5", fragment8.getSharedConfigFragment().getContent().trim());
    }

    @Test
    public void hash() {
        String plugin1 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "    {\n" +
                "      \"pre_condition\": [\n" +
                "        {\n" +
                "          \"custom_extractor\": \"Header[plugin1]\",\n" +
                "          \"operator\": \"present\",\n" +
                "          \"invert\": true\n" +
                "        },\n" +
                "        {\n" +
                "          \"custom_extractor\": \"Header[plugin2]\",\n" +
                "          \"operator\": \"=\",\n" +
                "          \"right_value\": \"ratelimit\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"hour\": 1,\n" +
                "      \"second\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";


        FragmentHolder fragment1 = processor.process(plugin1, serviceInfo);
        FragmentHolder fragment2 = processor.process(plugin1, serviceInfo);
        Assert.assertEquals(fragment1.getSharedConfigFragment().getContent(),fragment2.getSharedConfigFragment().getContent());
    }
}