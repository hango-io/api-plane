package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.hango.cloud.meta.ServiceInfo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xin li
 * @date 2022/8/9 15:11
 */
public class WafProcessorTest extends BasePluginTest {
    @Autowired
    WafProcessor processor = new WafProcessor();

    @Test
    public void process() {
        String p1 = "{\n" +
                "  \"wafRule\": {\n" +
                "    \"dosSwitch\":true,\n" +
                "    \"dosConfig\": {\n" +
                "      \"dos_burst_time_slice\": 2,\n" +
                "      \"dos_counter_threshold\": 200,\n" +
                "      \"dos_block_timeout\": 1\n" +
                "    },\n" +
                "    \"scannerSwitch\":true,\n" +
                "    \"lfiSwitch\":false,\n" +
                "    \"rfiSwitch\":false,\n" +
                "    \"rceSwitch\":false,\n" +
                "    \"phpInjectionSwitch\":false,\n" +
                "    \"xssSwitch\":false,\n" +
                "    \"sqliSwitch\":false,\n" +
                "    \"sessionFixationSwitch\":false,\n" +
                "    \"javaInjectionSwitch\":false,\n" +
                "    \"cgiDataLeakagesSwitch\":false,\n" +
                "    \"sqlDataLeakagesSwitch\":false,\n" +
                "    \"javaDataLeakagesSwitch\":false,\n" +
                "    \"phpDataLeakagesSwitch\":false,\n" +
                "    \"iisDataLeakagesSwitch\":false\n" +
                "  }\n" +
                "}";

        FragmentHolder f = processor.process(p1, new ServiceInfo());
        //TODO 因为使用Hashmap重新组织的config，生产的yml顺序会乱掉，固定为如下格式暂时先以该方式通过单测，后续待前端优化后需修复此单测。
        String expected = "waf_rule:\n" +
                "- waf_rule_path: \"/etc/envoy/waf/REQUEST-912-DOS-PROTECTION.conf\"\n" +
                "  config:\n" +
                "    tx.dos_block_timeout: 1\n" +
                "    tx.dos_burst_time_slice: 2\n" +
                "    tx.dos_counter_threshold: 200\n" +
                "- waf_rule_path: \"/etc/envoy/waf/REQUEST-913-SCANNER-DETECTION.conf\"";
        String actual = f.getVirtualServiceFragment().getContent().trim();
        System.out.println("================================================================================================");
        System.out.println("expected value is :" + expected);
        System.out.println("================================================================================================");
        System.out.println("actual value is :" + actual);
        System.out.println("================================================================================================");
        Assert.assertEquals(expected, actual);
    }
}
