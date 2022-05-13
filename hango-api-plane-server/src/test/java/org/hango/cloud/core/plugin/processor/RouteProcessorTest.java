package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RouteProcessorTest extends BasePluginTest {

    @Autowired
    RouteProcessor routeProcessor;

    @Test
    public void process() {

        String p1 = "{\n" +
                "  \"kind\": \"ianus-router\",\n" +
                "  \"rule\": [\n" +
                "  {\n" +
                "    \"name\": \"rewrite\",\n" +
                "    \"matcher\": [\n" +
                "    {\n" +
                "      \"source_type\": \"Header\",\n" +
                "      \"left_value\": \"plugin\",\n" +
                "      \"op\": \"=\",\n" +
                "      \"right_value\": \"rewrite\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"action\": {\n" +
                "      \"action_type\": \"rewrite\",\n" +
                "      \"rewrite_regex\": \"/rewrite/{group1}/{group2}\",\n" +
                "      \"target\": \"/anything/{{group2}}/{{group1}}\"\n" +
                "    }\n" +
                "  }\n" +
                "  ]\n" +
                "}";


        String p2 = "{\n" +
                "  \"kind\": \"ianus-router\",\n" +
                "  \"rule\": [\n" +
                "  {\n" +
                "    \"name\": \"rewrite\",\n" +
                "    \"matcher\": [\n" +
                "    {\n" +
                "      \"source_type\": \"Header\",\n" +
                "      \"left_value\": \"plugin\",\n" +
                "      \"op\": \"=\",\n" +
                "      \"right_value\": \"rewrite\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"action\": {\n" +
                "      \"action_type\": \"rewrite\",\n" +
                "      \"rewrite_regex\": \"/rewrite/(.*)/(.*)\",\n" +
                "      \"target\": \"/anything/$2/$1\"\n" +
                "    }\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String p3 = "{\n" +
                "  \"kind\": \"ianus-router\",\n" +
                "  \"rule\": [\n" +
                "  {\n" +
                "    \"name\": \"redirect\",\n" +
                "    \"matcher\": [\n" +
                "    {\n" +
                "      \"source_type\": \"Header\",\n" +
                "      \"left_value\": \"plugin\",\n" +
                "      \"op\": \"=\",\n" +
                "      \"right_value\": \"redirect\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"action\": {\n" +
                "      \"action_type\": \"redirect\",\n" +
                "      \"target\": \"/anything/redirect\"\n" +
                "    }\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String p4 = "{\n" +
                "  \"kind\": \"ianus-router\",\n" +
                "  \"rule\": [\n" +
                "  {\n" +
                "    \"name\": \"return\",\n" +
                "    \"matcher\": [\n" +
                "    {\n" +
                "      \"source_type\": \"Header\",\n" +
                "      \"left_value\": \"plugin\",\n" +
                "      \"op\": \"=\",\n" +
                "      \"right_value\": \"return\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"action\": {\n" +
                "      \"action_type\": \"return\",\n" +
                "      \"return_target\": {\n" +
                "        \"code\": 403,\n" +
                "        \"header\": [\n" +
                "        {\n" +
                "          \"name\":\"Content-Type\",\n" +
                "          \"value\": \"application/json\"\n" +
                "        }\n" +
                "        ],\n" +
                "        \"body\": \"{\\\"abc\\\":\\\"def\\\"}\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String p5 = "{\n" +
                "  \"kind\": \"ianus-router\",\n" +
                "  \"rule\": [\n" +
                "    {\n" +
                "      \"name\": \"return\",\n" +
                "      \"matcher\": [],\n" +
                "      \"action\": {\n" +
                "        \"action_type\": \"return\",\n" +
                "        \"return_target\": {\n" +
                "          \"code\": 403,\n" +
                "          \"header\": [\n" +
                "            {\n" +
                "              \"name\": \"Content-Type\",\n" +
                "              \"value\": \"application/json\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"body\": \"{\\\"abc\\\":\\\"def\\\"}\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        FragmentHolder f1 = routeProcessor.process(p1, serviceInfo);
        FragmentHolder f2 = routeProcessor.process(p2, serviceInfo);
        FragmentHolder f3 = routeProcessor.process(p3, serviceInfo);
        FragmentHolder f4 = routeProcessor.process(p4, serviceInfo);
        FragmentHolder f5 = routeProcessor.process(p5, serviceInfo);
        //TODO
    }

}