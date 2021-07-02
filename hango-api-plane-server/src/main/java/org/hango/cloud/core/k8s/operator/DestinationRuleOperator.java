package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.core.editor.PathExpressionEnum;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleSpec;
import me.snowdrop.istio.api.networking.v1alpha3.Subset;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
public class DestinationRuleOperator implements k8sResourceOperator<DestinationRule> {

    @Override
    public DestinationRule merge(DestinationRule old, DestinationRule fresh) {

        DestinationRuleSpec oldSpec = old.getSpec();
        DestinationRuleSpec freshSpec = fresh.getSpec();

        DestinationRule latest = new DestinationRuleBuilder(old).build();
        DestinationRuleSpec latestSpec = latest.getSpec();
        latestSpec.setHost(freshSpec.getHost());
        latestSpec.setTrafficPolicy(freshSpec.getTrafficPolicy());
        latestSpec.setSubsets(mergeList(oldSpec.getSubsets(), freshSpec.getSubsets(), new SubsetEquals()));
        return latest;
    }

    private class SubsetEquals implements Equals<Subset> {

        @Override
        public boolean apply(Subset ot, Subset nt) {
            return Objects.equals(ot.getName(), nt.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.DestinationRule.name().equals(name);
    }

    @Override
    public boolean isUseless(DestinationRule destinationRule) {
        return destinationRule == null ||
                StringUtils.isEmpty(destinationRule.getApiVersion()) ||
                 destinationRule.getSpec() == null ||
                  CollectionUtils.isEmpty(destinationRule.getSpec().getSubsets());
    }

    @Override
    public DestinationRule subtract(DestinationRule old, String value) {
        ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_NAME.translate(value));
        return gen.object(DestinationRule.class);
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
