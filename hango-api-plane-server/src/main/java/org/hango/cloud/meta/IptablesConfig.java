package org.hango.cloud.meta;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/3/27
 */
public class IptablesConfig {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private boolean enableOutbound;
	private List<String> outboundIps;
	private List<String> excludeOutboundIps;
	private List<String> outboundPorts;

	private boolean enableInbound;
	private List<String> inboundPorts;

	public IptablesConfig(){}

	public IptablesConfig(boolean enableOutbound, List<String> outboundIps, List<String> excludeOutboundIps, List<String> outboundPorts, boolean enableInbound, List<String> inboundPorts) {
		this.enableOutbound = enableOutbound;
		this.outboundIps = outboundIps;
		this.excludeOutboundIps = excludeOutboundIps;
		this.outboundPorts = outboundPorts;
		this.enableInbound = enableInbound;
		this.inboundPorts = inboundPorts;
	}

	public boolean isEnableOutbound() {
		return enableOutbound;
	}

	public void setEnableOutbound(boolean enableOutbound) {
		this.enableOutbound = enableOutbound;
	}

	public List<String> getOutboundIps() {
		return outboundIps;
	}

	public void setOutboundIps(List<String> outboundIps) {
		this.outboundIps = outboundIps;
	}

	public List<String> getOutboundPorts() {
		return outboundPorts;
	}

	public void setOutboundPorts(List<String> outboundPorts) {
		this.outboundPorts = outboundPorts;
	}

	public boolean isEnableInbound() {
		return enableInbound;
	}

	public void setEnableInbound(boolean enableInbound) {
		this.enableInbound = enableInbound;
	}

	public List<String> getInboundPorts() {
		return inboundPorts;
	}

	public void setInboundPorts(List<String> inboundPorts) {
		this.inboundPorts = inboundPorts;
	}

	public List<String> getExcludeOutboundIps() {
		return excludeOutboundIps;
	}

	public void setExcludeOutboundIps(List<String> excludeOutboundIps) {
		this.excludeOutboundIps = excludeOutboundIps;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append(asJson("enableOutbound"))
			.append(": ")
			.append(enableOutbound);
		appendList(sb, "outboundIps", outboundIps);
		appendList(sb, "excludeOutboundIps", excludeOutboundIps);
		appendList(sb, "outboundPorts", outboundPorts);
		sb.append(", ")
			.append(asJson("enableInbound"))
			.append(": ")
			.append(enableInbound);
		appendList(sb, "inboundPorts", inboundPorts);
		return sb.append('}').toString();
	}

	private void appendList(StringBuilder sb, String key, List<String> values) {
		if (values != null) {
			sb.append(", ")
				.append(asJson(key))
				.append(": ")
				.append(asJson(values));
		}
	}

	private String asJson(List<String> strings) {
		return strings.stream()
			.map(this::asJson)
			.collect(Collectors.joining(", ", "[", "]"));
	}

	private String asJson(String obj) {
		return String.format("\"%s\"", obj);
	}

	public static IptablesConfig readFromJson(String json) {
		if (json == null) {
			return null;
		}
		try {
			IptablesConfig result = MAPPER.readValue(json, IptablesConfig.class);
			result.setOutboundPorts(convertPorts(result.getOutboundPorts()));
			result.setInboundPorts(convertPorts(result.getInboundPorts()));
			return result;
		} catch (IOException e) {
			return null;
		}
	}

	private static List<String> convertPorts(List<String> ports) {
		if (ports == null) {
			return null;
		} else {
			return ports.stream()
				.map(p -> p.replaceAll(":", "-"))
				.collect(Collectors.toList());
		}
	}

}
