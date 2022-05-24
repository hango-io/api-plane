package org.hango.cloud.mcp;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 *
 * @date 2020/4/30
 **/
public class McpUtilsTest {

    @Test
    public void testLabel() {
        Map<String, String> m1 = new HashMap<>();
        m1.put("b", "dads");
        m1.put("kop", "ddd");
        m1.put("*sd", null);
        m1.put("-", "2");
        m1.put("1", "7");
        String label = McpUtils.getLabel(m1);
        String labelMatch = McpUtils.getLabelMatch(m1);
        Assert.assertEquals("{[*sd,null][-,2][1,7][b,dads][kop,ddd]}", label);
        Assert.assertEquals("{%[*sd,null]%[-,2]%[1,7]%[b,dads]%[kop,ddd]%}", labelMatch);

        Map<String, String> m2 = new HashMap<>();
        String label2 = McpUtils.getLabel(m2);
        String labelMatch2 = McpUtils.getLabelMatch(m2);
        Assert.assertEquals("{}", label2);
        Assert.assertEquals("{%}", labelMatch2);

        String label3 = McpUtils.getLabel(null);
        String labelMatch3 = McpUtils.getLabelMatch(null);
        Assert.assertEquals("{}", label3);
        Assert.assertEquals("{%}", labelMatch3);
    }

    @Test
    public void testGetResourceName() {
        Assert.assertThat(McpUtils.getResourceName(null, null), equalTo(""));
        Assert.assertThat(McpUtils.getResourceName("gateway-system", null), equalTo("gateway-system/"));
        Assert.assertThat(McpUtils.getResourceName(null, "testFile"), equalTo("testFile"));
        Assert.assertThat(McpUtils.getResourceName("gateway-system", "testFile"), equalTo("gateway-system/testFile"));
    }
}
