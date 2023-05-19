package org.hango.cloud.core.k8s;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.*;
import io.fabric8.kubernetes.client.utils.URLUtils;
import me.snowdrop.istio.api.authentication.v1alpha1.Policy;
import me.snowdrop.istio.api.authentication.v1alpha1.PolicyList;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.api.rbac.v1alpha1.*;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.util.exception.ApiPlaneException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Declare CRD definition
 */
@SuppressWarnings("java:S115")
public enum K8sResourceEnum {
    /** VirtualService resource */
    VirtualService(
            K8sTypes.VirtualService.class,
            K8sTypes.VirtualServiceList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/virtualservices")),
    /** DestinationRule resource */
    DestinationRule(
            K8sTypes.DestinationRule.class,
            K8sTypes.DestinationRuleList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/destinationrules")),
    /** DestinationRule resource */
    ServiceRole(
            ServiceRole.class,
            ServiceRoleList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/serviceroles")),
    /** DestinationRule resource */
    ServiceRoleBinding(
            ServiceRoleBinding.class,
            ServiceRoleBindingList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/servicerolebindings")),
    /** DestinationRule resource */
    Policy(
            Policy.class,
            PolicyList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/authentication.istio.io/v1alpha1/namespaces/%s/policies")),
    /** DestinationRule resource */
    ServiceAccount(
            ServiceAccount.class,
            ServiceAccountList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/serviceaccounts")),
    /** DestinationRule resource */
    Gateway(
            Gateway.class,
            GatewayList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/gateways")),
    /** DestinationRule resource */
    Pod(
            Pod.class,
            PodList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/pods")),
    /** DestinationRule resource */
    ClusterRbacConfig(
            RbacConfig.class,
            RbacConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/clusterrbacconfigs")),
    /** DestinationRule resource */
    RbacConfig(
            RbacConfig.class,
            RbacConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/clusterrbacconfigs")),
    /** DestinationRule resource */
    SharedConfig(
            SharedConfig.class,
            SharedConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/sharedconfigs")),
    /** DestinationRule resource */
    ServiceEntry(
            ServiceEntry.class,
            ServiceEntryList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/serviceentries")),
    /** DestinationRule resource */
    PluginManager(
            K8sTypes.PluginManager.class,
            K8sTypes.PluginManagerList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/microservice.slime.io/v1alpha1/namespaces/%s/pluginmanagers")),
    /** DestinationRule resource */
    EnvoyFilter(
            K8sTypes.EnvoyFilter.class,
            K8sTypes.EnvoyFilterList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/envoyfilters")),
    /** DestinationRule resource */
    Deployment(
            Deployment.class,
            DeploymentList.class,
            ImmutableMap.of(
                    K8sVersion.V1_11_0, "/apis/extensions/v1beta1/namespaces/%s/deployments",
                    K8sVersion.V1_17_0, "/apis/apps/v1/namespaces/%s/deployments"
            )),
    /** DestinationRule resource */
    Endpoints(
            Endpoints.class,
            EndpointsList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/endpoints/")),
    /** DestinationRule resource */
    DaemonSet(
            DaemonSet.class,
            DaemonSetList.class,
            ImmutableMap.of(
                    K8sVersion.V1_11_0, "/apis/extensions/v1beta1/namespaces/%s/daemonsets",
                    K8sVersion.V1_17_0, "/apis/apps/v1/namespaces/%s/daemonsets"
            )),
    /** DestinationRule resource */
    Service(
            Service.class,
            ServiceList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/services/")),
    /** DestinationRule resource */
    StatefulSet(
            StatefulSet.class,
            StatefulSetList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/apps/v1/namespaces/%s/statefulsets/")),
    /** DestinationRule resource */
    ReplicaSet(
            ReplicaSet.class,
            ReplicaSetList.class,
            ImmutableMap.of(
                    K8sVersion.V1_11_0, "/apis/extensions/v1beta1/namespaces/%s/replicasets/",
                    K8sVersion.V1_17_0, "/apis/apps/v1/namespaces/%s/replicasets/"
            )),
    /** DestinationRule resource */
    VersionManager(
            VersionManager.class,
            VersionManagerList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/versionmanagers")),
    /** DestinationRule resource */
    GlobalConfig(
            GlobalConfig.class,
            GlobalConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/globalconfigs")
    ),
    /** DestinationRule resource */
    NameSpace(
            Namespace.class,
            NamespaceList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s")),
    /** DestinationRule resource */
    EnvoyPlugin(
            K8sTypes.EnvoyPlugin.class,
            K8sTypes.EnvoyPluginList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/microservice.slime.io/v1alpha1/namespaces/%s/envoyplugins")),
    /** DestinationRule resource */
    MixerUrlPattern(
            MixerUrlPattern.class,
            MixerUrlPatternList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/mixerurlpatterns")),
    /** DestinationRule resource */
    ConfigMap(
            ConfigMap.class,
            ConfigMapList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/configmaps")),
    /** DestinationRule resource */
    SmartLimiter(
            K8sTypes.SmartLimiter.class,
            K8sTypes.SmartLimiterList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/microservice.slime.io/v1alpha2/namespaces/%s/smartlimiters")),
    /** DestinationRule resource */
    Sidecar(
            me.snowdrop.istio.api.networking.v1alpha3.Sidecar.class,
            SidecarList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/sidecars")),
    Secret(
            Secret.class,
            SecretList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/secrets")),

    /** Ingress resource */
    Ingress(
            io.fabric8.kubernetes.api.model.extensions.Ingress.class,
            io.fabric8.kubernetes.api.model.extensions.IngressList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/extensions/v1beta1/namespaces/%s/ingresses")),
    //#######  Gateway API resource ###########
    KubernetesGateway(
            io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.Gateway.class,
            io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.GatewayList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/gateway.networking.k8s.io/v1beta1/namespaces/%s/gateways")),

    HTTPRoute(
            io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute.class,
            io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRouteList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/gateway.networking.k8s.io/v1beta1/namespaces/%s/httproutes"))

    ;

    private Class<? extends HasMetadata> mappingType;
    private Class<? extends KubernetesResourceList> mappingListType;
    private String selfLink;
    private Boolean isClustered;

    K8sResourceEnum(Class<? extends HasMetadata> mappingType, Class<? extends KubernetesResourceList> mappingListType, Map<K8sVersion, String> selfLinkMap) {
        // 选择接近当前k8s版本但小于当前版本的selfLink
        K8sVersion currentVersion;
        String currentK8sVersion = System.getProperty("k8sVersion");
        if (StringUtils.isNotEmpty(currentK8sVersion)) {
            currentVersion = new K8sVersion(currentK8sVersion);
        } else {
            currentVersion = K8sVersion.V1_11_0;
        }
        K8sVersion closedVersion = select(selfLinkMap.keySet(), currentVersion);

        this.mappingType = mappingType;
        this.mappingListType = mappingListType;
        this.selfLink = selfLinkMap.get(closedVersion);
        this.isClustered = false;
    }

    public String selfLink() {
        return selfLink;
    }

    public String selfLink(String namespace) {
        //FIXME 后续可以使用优雅些的方式
        if (StringUtils.isEmpty(namespace)) {
            return selfLink.replace("namespaces/%s/", "");
        }
        return selfLink.contains("%s") ? String.format(selfLink, namespace) : selfLink;
    }

    public String selfLink(String masterUrl, String namespace) {
        return URLUtils.pathJoin(masterUrl, selfLink(namespace));
    }

    public Boolean isClustered() {
        return isClustered;
    }

    public Boolean isNamespaced() {
        return !isClustered;
    }

    public Class<? extends HasMetadata> mappingType() {
        return mappingType;
    }

    public Class<? extends KubernetesResourceList> mappingListType() {
        return mappingListType;
    }

    public static K8sResourceEnum getItem(String name) {
        Pattern pattern = Pattern.compile("(.*)List$");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return get(matcher.group(1));
        }
        return get(name);
    }

    public static K8sResourceEnum get(String name) {
        for (K8sResourceEnum k8sResourceEnum : values()) {
            if (k8sResourceEnum.name().equalsIgnoreCase(name)) {
                return k8sResourceEnum;
            }
        }
        throw new ApiPlaneException("Unsupported resource types: " + name);
    }

    /**
     * 查找最接近当前k8s版本但小于当前k8s版本的 k8sVersion
     * 例如：
     * 1. k8sVersions[v1.1.0, v1.7.0], currentVersion:v1.1.0，返回k8sVersion:v1.1.0
     * 2. k8sVersions[v1.5.0, v1.7.0], currentVersion:v1.1.0，找不到合适版本，报错
     * 3. k8sVersions[v1.5.0, v1.7.0], currentVersion:v1.6.0，返回k8sVersion:v1.5.0
     * 4. k8sVersions[v1.5.0, v1.7.0], currentVersion:v1.8.0，返回k8sVersion:v1.7.0
     *
     * @param k8sVersions
     * @param currentVersion 当前k8sVersion
     * @return
     */
    private K8sVersion select(Collection<K8sVersion> k8sVersions, K8sVersion currentVersion) {
        K8sVersion[] sortedVersion = k8sVersions.toArray(new K8sVersion[0]);
        Arrays.sort(sortedVersion);
        int length = sortedVersion.length;
        for (int i = 0; i < length; i++) {
            int compare = sortedVersion[i].compareTo(currentVersion);
            if (compare < 0) {
                if (i + 1 == length) {
                    return sortedVersion[i];
                }
            } else if (compare > 0) {
                if (i == 0) {
                    throw new RuntimeException(String.format("crd:%s are not compatible with the current K8S version: %s", this.name(), currentVersion));
                } else {
                    return sortedVersion[i - 1];
                }
            } else {
                return sortedVersion[i];
            }
        }
        throw new RuntimeException(String.format("crd:%s are not compatible with the current K8S version: %s", this.name(), currentVersion));
    }
}
