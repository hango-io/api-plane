package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @date 2020/4/16
 **/
public class MeshRateLimitProcessorTest extends BasePluginTest {
    @Autowired
    MeshRateLimitProcessor processor;

    @Test
    public void processor() {
        String plugin1 = "{\n" +
                "  \"kind\": \"mesh-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "    {\n" +
                "      \"type\":\"local\",\n" +
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
                "      \"second\": 2,\n" +
                "      \"minute\": 3,\n" +
                "      \"day\": 4,\n" +
                "      \"when\": \"true\",\n" +
                "      \"then\": \"@/{pod}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String plugin2 = "{\n" +
                "  \"kind\": \"mesh-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"type\":\"local\",\n" +
                "    \"hour\": 1,\n" +
                "    \"second\": 2,\n" +
                "    \"minute\": 3,\n" +
                "    \"day\": 4,\n" +
                "    \"when\": \"true\",\n" +
                "    \"then\": \"@/{pod}\"\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String plugin3 = "{\n" +
                "  \"kind\": \"mesh-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "    {\n" +
                "      \"type\":\"local\",\n" +
                "      \"identifier_extractor\": \"Header[plugin]\",\n" +
                "      \"hour\": 1,\n" +
                "      \"second\": 2,\n" +
                "      \"minute\": 3,\n" +
                "      \"day\": 4,\n" +
                "      \"when\": \"true\",\n" +
                "      \"then\": \"@/{pod}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String plugin4 = "{\n" +
                "  \"kind\": \"mesh-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "    {\n" +
                "      \"type\":\"global\",\n" +
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
                "      \"second\": 2,\n" +
                "      \"minute\": 3,\n" +
                "      \"day\": 4\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String plugin5 = "";
        FragmentHolder fragment1 = processor.process(plugin1, serviceInfo);
        FragmentHolder fragment2 = processor.process(plugin2, serviceInfo);
        FragmentHolder fragment3 = processor.process(plugin3, serviceInfo);
        FragmentHolder fragment4 = processor.process(plugin4, serviceInfo);
    }
}
