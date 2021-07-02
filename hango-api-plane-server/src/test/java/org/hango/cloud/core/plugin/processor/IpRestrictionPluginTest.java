package org.hango.cloud.core.plugin.processor;

import org.hango.cloud.core.plugin.FragmentHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class IpRestrictionPluginTest extends BasePluginTest{

	@Autowired
	AggregateExtensionProcessor aggregateExtensionProcessor;
	@Autowired
    IpRestrictionProcessor ipRestrictionProcessor;

	@Test
	public void process(){
		String p = "{\"type\":\"0\",\"list\":[\"127.0.0.1\"],\"kind\":\"ip-restriction\"}";

		FragmentHolder f = aggregateExtensionProcessor.process(p, serviceInfo);

		Assert.assertEquals("name: \"proxy.filters.http.iprestriction\"\n" + "inline:\n" + "  settings:\n" + "    "
		                    + "list:\n"
		                    + "    - \"127.0.0.1\"\n" + "    type: \"BLACK\"",
		                    f.getVirtualServiceFragment().getContent().trim());
	}
}

