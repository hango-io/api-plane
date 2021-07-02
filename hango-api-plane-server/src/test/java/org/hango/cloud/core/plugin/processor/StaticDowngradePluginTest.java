package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class StaticDowngradePluginTest extends BasePluginTest{

	@Autowired
	AggregateExtensionProcessor aggregateExtensionProcessor;
	@Autowired
    StaticDowngradeProcessor staticDowngradeProcessor;

	@Test
	public void process(){
		String p = "{\"condition\":{\"request\":{\"requestSwitch\":true,\"path\":{\"match_type\":\"exact_match\","
		           + "\"value\":\"/unittest\"},\"host\":{\"match_type\":\"exact_match\",\"value\":\"www.hango.com\"},"
		           + "\"headers\":[{\"headerKey\":\"abc\",\"match_type\":\"exact_match\",\"value\":\"123\"}],"
		           + "\"method\":[\"GET\"]},\"response\":{\"code\":{\"match_type\":\"exact_match\",\"value\":\"503\"},"
		           + "\"headers\":[{\"headerKey\":\"aaa\",\"match_type\":\"exact_match\",\"value\":\"123\"}]}},"
		           + "\"kind\":\"static-downgrade\",\"response\":{\"headers\":{\"static\":\"downgrade\"},"
		           + "\"code\":\"200\",\"body\":\"static-downgrade\"}}";

		FragmentHolder f = aggregateExtensionProcessor.process(p, serviceInfo);
		Assert.assertEquals("name: \"proxy.filters.http.staticdowngrade\"\n" + "inline:\n" + "  settings:\n"
		                    + "    downgrade_rpx:\n" + "      headers:\n" + "      - name: \"aaa\"\n"
		                    + "        exact_match: \"123\"\n" + "      - name: \":status\"\n"
		                    + "        safe_regex_match:\n" + "          regex: \"503|\"\n" + "    static_response:\n"
		                    + "      http_status: 200\n" + "      headers:\n" + "      - key: \"static\"\n"
		                    + "        value: \"downgrade\"\n" + "      body:\n"
		                    + "        inline_string: \"static-downgrade\"\n" + "    downgrade_rqx:\n"
		                    + "      headers:\n" + "      - name: \"abc\"\n" + "        exact_match: \"123\"\n"
		                    + "      - name: \":authority\"\n" + "        exact_match: \"www.hango.com\"\n"
		                    + "      - name: \":method\"\n" + "        exact_match: \"GET\"\n"
		                    + "      - name: \":path\"\n" + "        exact_match: \"/unittest\"", f.getVirtualServiceFragment().getContent().trim());
	}
}

