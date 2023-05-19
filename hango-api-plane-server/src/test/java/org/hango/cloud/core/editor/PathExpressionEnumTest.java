package org.hango.cloud.core.editor;

import com.jayway.jsonpath.JsonPath;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@SuppressWarnings({"java:S1192"})
public class PathExpressionEnumTest {

    @Test
    public void testPathExpression() {

        String json = "{\n" +
                "  \"descriptors\": [\n" +
                "    {\n" +
                "      \"api\": 116,\n" +
                "      \"key\": \"header_match\",\n" +
                "      \"rateLimit\": {\n" +
                "        \"requestsPerUnit\": 5,\n" +
                "        \"unit\": \"MINUTE\"\n" +
                "      },\n" +
                "      \"value\": \"Service[qz]-User[none]-Gateway[gw1]-Api[116]-Id[1727ff10-fbf3-46fd-a577-327ff48d0674]\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"api\": 116,\n" +
                "      \"key\": \"header_match\",\n" +
                "      \"rateLimit\": {\n" +
                "        \"requestsPerUnit\": 5,\n" +
                "        \"unit\": \"HOUR\"\n" +
                "      },\n" +
                "      \"value\": \"Service[qz]-User[none]-Gateway[gw1]-Api[116]-Id[35ab0f33-6a8e-438a-bb40-922d1e817cec]\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"key\": \"header_match\",\n" +
                "      \"rateLimit\": {\n" +
                "        \"requestsPerUnit\": 1,\n" +
                "        \"unit\": \"HOUR\"\n" +
                "      },\n" +
                "      \"value\": \"Service[httpbin]-User[none]-Gateway[gw2]-Api[httpbin]-Id[08638e47-48db-43bc-9c21-07ef892b5494]\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"domain\": \"qingzhou\"\n" +
                "}";

        List<String> desc1 = JsonPath.read(json,
                PathExpressionEnum.REMOVE_GATEWAY_RATELIMIT_CONFIGMAP_BY_VALUE.translate("gw1", "116"));

        Assert.assertEquals(2, desc1.size());

        List<String> desc2 = JsonPath.read(json,
                PathExpressionEnum.REMOVE_GATEWAY_RATELIMIT_CONFIGMAP_BY_VALUE.translate("gw2", "httpbin"));

        Assert.assertEquals(1, desc2.size());
    }
}