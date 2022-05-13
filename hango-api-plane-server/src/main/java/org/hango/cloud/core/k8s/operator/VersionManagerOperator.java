package org.hango.cloud.core.k8s.operator;

import org.hango.cloud.configuration.ext.MeshConfig;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.meta.IptablesConfig;
import org.hango.cloud.meta.PodStatus;
import org.hango.cloud.meta.PodVersion;
import org.hango.cloud.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
public class VersionManagerOperator implements k8sResourceOperator<VersionManager> {

    @Autowired
    private MeshConfig meshConfig;

    @Override
    public VersionManager merge(VersionManager old, VersionManager fresh) {
        VersionManager versionManager = new VersionManagerBuilder(old).build();
        List<SidecarVersionSpec> oldSpecList  = old.getSpec().getSidecarVersionSpec();
        List<SidecarVersionSpec> latestSpecList  = fresh.getSpec().getSidecarVersionSpec();
        Map<String,String> oldLabel = old.getMetadata().getLabels();
        Map<String,String> freshLabel = fresh.getMetadata().getLabels();
        Map mergedLabel = mergeMap(oldLabel, freshLabel, (o, n) -> o.equals(n));
        versionManager.getSpec().setSidecarVersionSpec(mergeList(oldSpecList, latestSpecList, new SidecarVersionSpecEquals()));
        versionManager.getSpec().setStatus(old.getSpec().getStatus());
        //versionManager.getMetadata().setResourceVersion(old.getMetadata().getResourceVersion());
        versionManager.getMetadata().setResourceVersion(null);
        versionManager.getMetadata().setLabels(mergedLabel);
        return versionManager;
    }

    private class SidecarVersionSpecEquals implements Equals<SidecarVersionSpec> {
        @Override
        public boolean apply(SidecarVersionSpec np, SidecarVersionSpec op) {
            boolean isEqual = Objects.equals(op.getSelector(), np.getSelector());
            if (isEqual) {
                mergeToNewSidecarVersionSpec(np, op);
            }
            return isEqual;
        }

        private void mergeToNewSidecarVersionSpec(SidecarVersionSpec np, SidecarVersionSpec op) {
            if (StringUtils.isEmpty(np.getIptablesParams()) && StringUtils.isEmpty(np.getIptablesDetail())) {
                np.setIptablesParams(op.getIptablesParams());
                np.setIptablesDetail(op.getIptablesDetail());
            }
            if (np.getExpectedVersion() == null) {
                np.setExpectedVersion(op.getExpectedVersion());
            }
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.VersionManager.name().equals(name);
    }

    @Override
    public VersionManager subtract(VersionManager old, String value) {
        // TODO
        return old;
    }


    @Override
    public boolean isUseless(VersionManager versionManager) {
        return versionManager.getSpec() == null ||
                StringUtils.isEmpty(versionManager.getApiVersion()) ||
                CollectionUtils.isEmpty(versionManager.getSpec().getSidecarVersionSpec());
    }

    public List<PodStatus> getPodVersion(PodVersion podVersion, VersionManager versionmanager) {

        List<PodStatus> resultList = new ArrayList<>();
        Status state = versionmanager.getSpec().getStatus();
        if (state == null || CollectionUtils.isEmpty(state.getPodVersionStatus())) {
            return resultList;
        }

        List<PodVersionStatus> versionList  = state.getPodVersionStatus();
        if (CollectionUtils.isEmpty(versionList)) {
            return resultList;
        }
        List<String> podList = podVersion.getPodNames();

        for (String need : podList) {
            for (PodVersionStatus had : versionList) {
                if (had.getPodName().equals(need)) {
                    resultList.add(
                            new PodStatus(had.getPodName(),
                            had.getCurrentVersion(),
                            had.getExpectedVersion(),
                            had.getLastUpdateTime(),
                            had.getStatusCode(),
                            had.getStatusMessage()));
                    break;
                }
            }
        }
        return resultList;
    }

    public IptablesConfig getIptablesConfigOfApp(VersionManager vm, String appName) {
        List<SidecarVersionSpec> specs = vm.getSpec().getSidecarVersionSpec();
        if (specs == null || appName == null) {
            return null;
        }
        return specs.stream()
            .filter(spec -> {
                if (!(spec.getSelector() instanceof ViaLabelSelectorSelector)) {
                    return false;
                }
                ViaLabelSelectorSelector selector = (ViaLabelSelectorSelector) spec.getSelector();
                return selector.getViaLabelSelector() != null &&
                    selector.getViaLabelSelector().getLabels() != null &&
                    appName.equals(selector.getViaLabelSelector().getLabels().get(meshConfig.getSelectorAppKey()));
            })
            .map(spec -> IptablesConfig.readFromJson(spec.getIptablesDetail()))
            .findAny()
            .orElse(null);
    }

    public String getExpectedVersion (VersionManager versionmanager, String workLoadType, String workLoadName ) {

        List<SidecarVersionSpec> specList = versionmanager.getSpec().getSidecarVersionSpec();
        SidecarVersionSpec.Selector target;
        switch (workLoadType) {
            case "Deployment" :
                ViaDeployment deploy = new ViaDeployment();
                deploy.setName(workLoadName);
                ViaDeploymentSelector deploySelector = new ViaDeploymentSelector();
                deploySelector.setViaDeployment(deploy);
                target = deploySelector;
                break;
            case "StatefulSet" :
                ViaStatefulSet statefulSet = new ViaStatefulSet();
                statefulSet.setName(workLoadName);
                ViaStatefulSetSelector statefulSetSelector = new ViaStatefulSetSelector();
                statefulSetSelector.setViaStatefulSet(statefulSet);
                target = statefulSetSelector;
                break;
            case "Service" :
                ViaService service = new ViaService();
                service.setName(workLoadName);
                ViaServiceSelector serviceSelector = new ViaServiceSelector();
                serviceSelector.setViaService(service);
                target = serviceSelector;
                break;
            case "LabelSelector" :
                System.out.println("查询类型暂时不支持");
                return null;
            default :
                System.out.println("查询类型不存在");
                return null;

        }

        for (SidecarVersionSpec spec : specList) {
            SidecarVersionSpec.Selector selector = spec.getSelector();
            if (selector.equals(target)) {
                return spec.getExpectedVersion();
            }
        }

        return null;
    }
}
