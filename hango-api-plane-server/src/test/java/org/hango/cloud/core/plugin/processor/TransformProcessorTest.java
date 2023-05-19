package org.hango.cloud.core.plugin.processor;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TransformProcessorTest extends BasePluginTest {

    @Autowired
    TransformProcessor transformProcessor;

    @Test
    public void process() {

        String p1 = "{\n" +
                "  \"kind\":\"transformer\",\n" +
                "  \"headers\": [\n" +
                "  {\n" +
                "    \"key\": \"addHeaders\",\n" +
                "    \"text\": \"group1:{{url[0]}},group2:{{url[1]}}\",\n" +
                "    \"action\": \"Action_Default\"\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String p2 = "{\n" +
                "  \"kind\":\"ianus-request-transformer\",\n" +
                "  \"conditions\":{\n" +
                "    \"headers\":[\n" +
                "    {\n" +
                "      \"key\":\"condition1\",\n" +
                "      \"text\":\"aaa\",\n" +
                "      \"op\":\"prefix\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"querystrings\":[\n" +
                "    {\n" +
                "      \"key\":\"condition1\",\n" +
                "      \"text\":\".*\",\n" +
                "      \"op\":\"regex\"\n" +
                "    }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"headers\": [\n" +
                "  {\n" +
                "    \"key\": \"addHeaders\",\n" +
                "    \"text\": \"addHeaders\",\n" +
                "    \"action\": \"Action_Default\"\n" +
                "  }\n" +
                "  ],\n" +
                "  \"querystrings\": [\n" +
                "  {\n" +
                "    \"key\": \"addQuerystrings\",\n" +
                "    \"text\": \"addQuerystrings\",\n" +
                "    \"action\": \"Action_Default\"\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        transformProcessor.process(p1, serviceInfo);
        transformProcessor.process(p2, serviceInfo);
        //TODO
    }
}