package org.hango.cloud.core.plugin.processor;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheProcessorTest extends BasePluginTest {

    @Autowired
    CacheProcessor cacheProcessor;

    @Test
    public void process() {

        String p1 = "{\n" +
                "  \"kind\": \"cache\",\n" +
                "  \"cache\": {\n" +
                "    \"cacheTtls\": {\n" +
                "      \"LocalHttpCache\": {\n" +
                "        \"default\": 20000\n" +
                "      },\n" +
                "      \"RedisHttpCache\": {\n" +
                "        \"default\": 30000,\n" +
                "        \"customs\": {\n" +
                "          \"200\": 50000,\n" +
                "          \"5..\": 10000\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"keyMaker\": {\n" +
                "      \"queryParams\": [\n" +
                "        \"id\"\n" +
                "      ],\n" +
                "      \"headersKeys\": [\n" +
                "        \"comefrom\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"lowLevelFill\": true\n" +
                "  }\n" +
                "}";

//        cacheProcessor.process(p1, serviceInfo);
    }
}