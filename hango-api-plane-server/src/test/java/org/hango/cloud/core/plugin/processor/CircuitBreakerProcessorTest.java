package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @date 2020/4/8
 **/
public class CircuitBreakerProcessorTest extends BasePluginTest {
    @Autowired
    CircuitBreakerProcessor processor;

    @Test
    public void process() {
        String p1 = "{\n" +
                "  \"kind\": \"circuit-breaker\",\n" +
                "  \"config\": {\n" +
                "    \"consecutive_slow_requests\": \"3\",\n" +
                "    \"average_response_time\": \"0.1\",\n" +
                "    \"min_request_amount\": \"3\",\n" +
                "    \"error_percent_threshold\": \"50\",\n" +
                "    \"break_duration\": \"50\",\n" +
                "    \"lookback_duration\": \"10\"\n" +
                "  },\n" +
                "  \"response\": {\n" +
                "    \"code\": \"200\",\n" +
                "    \"body\": \"{\\\"ba\\\":\\\"ba\\\"}\",\n" +
                "    \"headers\": [\n" +
                "      {\n" +
                "        \"key\": \"buhao\",\n" +
                "        \"value\": \"buhaoyabuhao\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        FragmentHolder fragment1 = processor.process(p1, serviceInfo);
    }
}
