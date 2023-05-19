package org.hango.cloud.k8s;

import istio.networking.v1alpha3.DestinationRuleOuterClass;
import istio.networking.v1alpha3.VirtualServiceOuterClass;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2021/9/10
 */
public class K8sTypes {

	public static class RichVirtualService extends K8sResource<slime.microservice.v1alpha1.Richvirtualservice.RichVirtualService> {}
	public static class RichVirtualServiceList extends K8sResourceList<RichVirtualService> {}
	public static class PluginManager extends K8sResource<slime.microservice.plugin.v1alpha1.PluginManagerOuterClass.PluginManager> {}
	public static class PluginManagerList extends K8sResourceList<PluginManager> {}

	public static class EnvoyPlugin extends K8sResource<slime.microservice.plugin.v1alpha1.EnvoyPluginOuterClass.EnvoyPlugin> {}
	public static class EnvoyPluginList extends K8sResourceList<EnvoyPlugin> {}

	public static class VirtualService extends K8sResource<VirtualServiceOuterClass.VirtualService> {}
	public static class VirtualServiceList extends K8sResourceList<VirtualService> {}
	public static class DestinationRule extends K8sResource<DestinationRuleOuterClass.DestinationRule> {}
	public static class DestinationRuleList extends K8sResourceList<DestinationRule> {}
	public static class EnvoyFilter extends K8sResource<istio.networking.v1alpha3.EnvoyFilterOuterClass.EnvoyFilter> {}
	public static class EnvoyFilterList extends K8sResourceList<EnvoyFilter> {}
	public static class SmartLimiter extends K8sResource<slime.microservice.limiter.v1alpha2.SmartLimiter.SmartLimiterSpec> {}
	public static class SmartLimiterList extends K8sResourceList<SmartLimiter> {}

	static {
		K8sResource.addKind("microservice.slime.io/v1alpha1", RichVirtualService.class);
		K8sResource.addKind("microservice.slime.io/v1alpha1", PluginManager.class);
		K8sResource.addKind("microservice.slime.io/v1alpha1", EnvoyPlugin.class);
		K8sResource.addKind("microservice.slime.io/v1alpha2", SmartLimiter.class);
		K8sResource.addKind("networking.istio.io/v1alpha3", VirtualService.class);
		K8sResource.addKind("networking.istio.io/v1alpha3", DestinationRule.class);
		K8sResource.addKind("networking.istio.io/v1alpha3", EnvoyFilter.class);
	}

}
