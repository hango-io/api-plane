package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PercentLimitPluginTest extends BasePluginTest{

	@Autowired
	AggregateExtensionProcessor aggregateExtensionProcessor;
	@Autowired
    PercentLimitProcessor percentLimitProcessor;

	@Test
	public void process(){
		String p = "{\"limit_percent\":\"10\",\"kind\":\"percent-limit\"}";

		FragmentHolder f = aggregateExtensionProcessor.process(p, serviceInfo);
		Assert.assertEquals("name: \"envoy.fault\"\n" + "inline:\n" + "  settings:\n" + "    abort:\n"
		                    + "      http_status: 429\n" + "      percentage:\n" + "        denominator: \"MILLION\"\n"
		                    + "        numerator: 100000", f.getVirtualServiceFragment().getContent().trim());
	}
}

