package org.hango.cloud.core;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.K8sResourceGenerator;
import org.hango.cloud.core.k8s.K8sResourcePack;
import org.hango.cloud.core.k8s.operator.IntegratedResourceOperator;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ExceptionConst;
import org.hango.cloud.util.function.Merger;
import org.hango.cloud.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class IstioModelEngine {

    private static final Logger logger = LoggerFactory.getLogger(IstioModelEngine.class);

    protected static final String NEVER_NULL = "NEVER_NULL";

    private IntegratedResourceOperator operator;

    @Autowired
    public IstioModelEngine(IntegratedResourceOperator operator) {
        this.operator = operator;
    }

    /**
     * 合并两个crd,新的和旧的重叠部分会用新的覆盖旧的
     *
     * @param old
     * @param fresh
     * @return
     */
    public HasMetadata merge(HasMetadata old, HasMetadata fresh) {
        if (fresh == null) return old;
        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        return operator.merge(old, fresh);
    }

    /**
     * 在已有的istio crd中删去对应api部分
     */
    public HasMetadata subtract(HasMetadata old, Map<String, String> values) {
        return operator.subtract(old, values.get(old.getKind()));
    }

    protected boolean isUseless(HasMetadata i) {
        return operator.isUseless(i);
    }


    protected HasMetadata str2HasMetadata(String str) {
        logger.info("raw resource: " + str);
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(str, ResourceType.YAML);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        HasMetadata hmd = gen.object(resourceEnum.mappingType());
        return hmd;
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws) {
        return generateK8sPack(raws, null, null, r -> r, this::str2HasMetadata, hsm -> hsm);
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws, Function<String, HasMetadata> transFun) {
        return generateK8sPack(raws, null, null, r -> r, transFun, hsm -> hsm);
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws, Subtracter subtracter) {
        return generateK8sPack(raws, null, subtracter, r -> r, this::str2HasMetadata, hsm -> hsm);
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws, Merger merger, Subtracter subtracter, Function<String, HasMetadata> transFun) {
        return generateK8sPack(raws, merger, subtracter, r -> r, transFun, hsm -> hsm);
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws, Merger merger, Subtracter subtracter, Function<String, String> preFun, Function<String, HasMetadata> transFun) {
        return generateK8sPack(raws, merger, subtracter, preFun, transFun, hsm -> hsm);
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws, Subtracter subtracter, Function<String, String> preFun, Function<HasMetadata, HasMetadata> postFun) {
        return generateK8sPack(raws, null, subtracter, preFun, this::str2HasMetadata, postFun);
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws, Function<String, String> preFun, Function<HasMetadata, HasMetadata> postFun) {
        return generateK8sPack(raws, null, null, preFun, this::str2HasMetadata, postFun);
    }

    protected List<K8sResourcePack> generateK8sPack(List<String> raws, Merger merger, Subtracter subtracter,
                                                  Function<String, String> preFun, Function<String, HasMetadata> transFun,
                                                  Function<HasMetadata, HasMetadata> postFun) {
        if (CollectionUtils.isEmpty(raws)) {
            return Collections.EMPTY_LIST;
        }

        return raws.stream().map(r -> preFun.apply(r))
                .map(r -> transFun.apply(r))
                .map(hsm -> postFun.apply(hsm))
                .map(hsm -> new K8sResourcePack(hsm, merger, subtracter))
                .collect(Collectors.toList());
    }

    public IntegratedResourceOperator getOperator() {
        return operator;
    }

    protected class EmptyResourceGenerator implements Function<String, HasMetadata> {

        private HasMetadata hmd;

        public EmptyResourceGenerator(HasMetadata hmd) {
            this.hmd = hmd;
        }

        @Override
        public HasMetadata apply(String s) {
            if (NEVER_NULL.equals(s)) return hmd;
            return str2HasMetadata(s);
        }
    }

    protected class DynamicResourceGenerator implements Function<String, HasMetadata> {

        private Supplier<HasMetadata> supplier;

        public DynamicResourceGenerator(Supplier supplier) {
            this.supplier = supplier;
        }

        @Override
        public HasMetadata apply(String s) {
            if (NEVER_NULL.equals(s)) return supplier.get();
            return str2HasMetadata(s);
        }
    }
}
