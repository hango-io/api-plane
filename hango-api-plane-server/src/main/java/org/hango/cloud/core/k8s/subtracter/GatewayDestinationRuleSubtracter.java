package org.hango.cloud.core.k8s.subtracter;

import org.hango.cloud.core.editor.PathExpressionEnum;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.util.function.Subtracter;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;

public class GatewayDestinationRuleSubtracter implements Subtracter<DestinationRule> {

    private String key;

    public GatewayDestinationRuleSubtracter(String key) {
        this.key = key;
    }

    @Override
    public DestinationRule subtract(DestinationRule old) {
        ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_NAME.translate(key));
        return gen.object(DestinationRule.class);
    }
}
