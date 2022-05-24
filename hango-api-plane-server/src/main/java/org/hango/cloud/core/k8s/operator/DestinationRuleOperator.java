package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.k8s.K8sTypes;
import org.hango.cloud.util.function.Equals;
import istio.networking.v1alpha3.DestinationRuleOuterClass;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DestinationRuleOperator implements k8sResourceOperator<K8sTypes.DestinationRule> {

    @Override
    public K8sTypes.DestinationRule merge(K8sTypes.DestinationRule old, K8sTypes.DestinationRule fresh) {
        DestinationRuleOuterClass.DestinationRule oldSpec = old.getSpec();
        DestinationRuleOuterClass.DestinationRule freshSpec = fresh.getSpec();

        K8sTypes.DestinationRule latest = new K8sTypes.DestinationRule();
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        latest.setMetadata(old.getMetadata());
        DestinationRuleOuterClass.DestinationRule.Builder builder = DestinationRuleOuterClass.DestinationRule.newBuilder();
        if (!StringUtils.isEmpty(freshSpec.getAltStatName())){
            builder.setAltStatName(freshSpec.getAltStatName());
        }
        if (!StringUtils.isEmpty(freshSpec.getHost())){
            builder.setHost(freshSpec.getHost());
        }

        List subList = mergeList(oldSpec.getSubsetsList(), freshSpec.getSubsetsList(), new SubsetEquals());
        if (!CollectionUtils.isEmpty(subList)){
            builder.addAllSubsets(subList);
        }
        builder.setTrafficPolicy(freshSpec.getTrafficPolicy());
        latest.setSpec(builder.build());
        return latest;

    }

    private class SubsetEquals implements Equals<DestinationRuleOuterClass.Subset> {

        @Override
        public boolean apply(DestinationRuleOuterClass.Subset ot, DestinationRuleOuterClass.Subset nt) {
            return Objects.equals(ot.getName(), nt.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.DestinationRule.name().equals(name);
    }

    @Override
    public boolean isUseless(K8sTypes.DestinationRule destinationRule) {
        return destinationRule == null ||
                StringUtils.isEmpty(destinationRule.getApiVersion()) ||
                 destinationRule.getSpec() == null ||
                  CollectionUtils.isEmpty(destinationRule.getSpec().getSubsetsList());
    }

    @Override
    public K8sTypes.DestinationRule subtract(K8sTypes.DestinationRule old, String value) {
        List<DestinationRuleOuterClass.Subset> subsets = old.getSpec().getSubsetsList().stream()
                .filter(subset -> !subset.getApi().equals(value))
                .collect(Collectors.toList());
        DestinationRuleOuterClass.DestinationRule build = old.getSpec().toBuilder().clearSubsets().build();
        old.setSpec((build.toBuilder().addAllSubsets(subsets).build()));
        return old;
    }

    /**
     * 在DestinationRule的Subset中加了api属性，根据service+api生成api对应值
     * @param service
     * @param api
     * @return
     */
    public String buildSubsetApi(String service, String api) {
        return String.format("%s-%s", service, api);
    }
}
