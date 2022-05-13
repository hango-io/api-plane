package org.hango.cloud.meta;

import java.util.Map;
import java.util.Set;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2021/5/31
 */
public class TrafficMarkConfigDto {

	private Set<String> serviceNames;
	private Set<String> enabledMarks;
	private Map<String, Set<String>> mappings;

	public Set<String> getServiceNames() {
		return serviceNames;
	}

	public void setServiceNames(Set<String> serviceNames) {
		this.serviceNames = serviceNames;
	}

	public Set<String> getEnabledMarks() {
		return enabledMarks;
	}

	public void setEnabledMarks(Set<String> enabledMarks) {
		this.enabledMarks = enabledMarks;
	}

	public Map<String, Set<String>> getMappings() {
		return mappings;
	}

	public void setMappings(Map<String, Set<String>> mappings) {
		this.mappings = mappings;
	}
}
