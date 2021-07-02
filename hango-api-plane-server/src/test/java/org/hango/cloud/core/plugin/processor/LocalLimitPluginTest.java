package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class LocalLimitPluginTest extends BasePluginTest{

	@Autowired
	AggregateExtensionProcessor aggregateExtensionProcessor;
	@Autowired
    LocalLimitProcessor localLimitProcessor;

	@Test
	public void process(){
		String p = "{\"limit_by_list\":[{\"headers\":[{\"headerKey\":\"abc\",\"match_type\":\"exact_match\","
		           + "\"value\":\"123\"}],\"day\":100,\"second\":55,\"hour\":55,\"minute\":55}],"
		           + "\"kind\":\"local-limiting\",\"name\":\"local-limiting\",\"IsSafe\":true}";

		FragmentHolder f = aggregateExtensionProcessor.process(p, serviceInfo);
		Assert.assertEquals("name: \"proxy.filters.http.locallimit\"\n" + "inline:\n" + "  settings:\n"
		                    + "    use_thread_local_token_bucket: true\n" + "    rate_limit:\n" + "    - config:\n"
		                    + "        unit: \"SS\"\n" + "        rate: 55\n" + "      matcher:\n"
		                    + "        headers:\n" + "        - name: \"abc\"\n" + "          exact_match: \"123\"\n"
		                    + "    - config:\n" + "        unit: \"MM\"\n" + "        rate: 55\n" + "      matcher:\n"
		                    + "        headers:\n" + "        - name: \"abc\"\n" + "          exact_match: \"123\"\n"
		                    + "    - config:\n" + "        unit: \"HH\"\n" + "        rate: 55\n" + "      matcher:\n"
		                    + "        headers:\n" + "        - name: \"abc\"\n" + "          exact_match: \"123\"\n"
		                    + "    - config:\n" + "        unit: \"DD\"\n" + "        rate: 100\n" + "      matcher:\n"
		                    + "        headers:\n" + "        - name: \"abc\"\n" + "          exact_match: \"123\"", f.getVirtualServiceFragment().getContent().trim());
	}
}

