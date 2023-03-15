package org.hango.cloud.service.impl;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.Gateway;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.Listener;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.ParentReference;
import org.apache.logging.log4j.util.Strings;
import org.hango.cloud.cache.K8sResourceCache;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.k8s.K8sResourceApiEnum;
import org.hango.cloud.meta.dto.KubernetesGatewayDTO;
import org.hango.cloud.service.KubernetesGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.core.template.TemplateConst.LABLE_ISTIO_REV;
import static org.hango.cloud.k8s.K8sResourceApiEnum.KubernetesGateway;

/**
 * @Author zhufengwei
 * @Date 2022/12/2
 */
@Service
public class KubernetesGatewayServiceImpl implements KubernetesGatewayService {
    private static final Logger log = LoggerFactory.getLogger(KubernetesGatewayServiceImpl.class);


    public static final String GATEWAY = "Gateway";

    public static final String PROJECT_KEY = "hango.io/gateway.project";

    @Autowired
    private K8sResourceCache k8sResourceCache;

    @Autowired
    private GlobalConfig globalConfig;

    @Override
    public List<KubernetesGatewayDTO> getKubernetesGateway(String gateway) {
        List<HasMetadata> resource = k8sResourceCache.getResourceByName(KubernetesGateway.name(), gateway);
        if (CollectionUtils.isEmpty(resource)){
            return new ArrayList<>();
        }
        return resource.stream().filter(this::revFilter).map(this::convertGateway).collect(Collectors.toList());

    }

    @Override
    public List<HTTPRoute> getHTTPRoute(String gateway) {
        List<HasMetadata> resource = k8sResourceCache.getResource(K8sResourceApiEnum.HTTPRoute.name());
        if (CollectionUtils.isEmpty(resource)){
            return new ArrayList<>();
        }
        return resource.stream().filter(this::revFilter).filter(o -> gatewayFilter(o, gateway)).map(this::convertHTTPRoute).collect(Collectors.toList());
    }

    private boolean revFilter(HasMetadata hasMetadata){
        Map<String, String> labels = hasMetadata.getMetadata().getLabels();
        return globalConfig.getIstioRev().equals(labels.get(LABLE_ISTIO_REV));
    }

    private boolean gatewayFilter(HasMetadata hasMetadata, String gatewayName){
        if (hasMetadata instanceof HTTPRoute){
            HTTPRoute httpRoute = (HTTPRoute)hasMetadata;
            List<ParentReference> parentRefs = httpRoute.getSpec().getParentRefs();
            if (CollectionUtils.isEmpty(parentRefs)){
                return false;
            }
            for (ParentReference parentRef : parentRefs) {
                if (GATEWAY.equals(parentRef.getKind()) && gatewayName.equals(parentRef.getName())){
                    return true;
                }
            }
        }
        return false;
    }


    private KubernetesGatewayDTO convertGateway(HasMetadata hasMetadata){
        if (!(hasMetadata instanceof Gateway)){
            return null;
        }
        Gateway gateway = (Gateway) hasMetadata;
        ObjectMeta metadata = gateway.getMetadata();
        KubernetesGatewayDTO gatewayDTO = new KubernetesGatewayDTO();
        gatewayDTO.setName(metadata.getName());
        Map<String, String> annotations = metadata.getAnnotations();
        if (annotations.containsKey(PROJECT_KEY)){
            String projectValue = annotations.get(PROJECT_KEY);
            gatewayDTO.setProjectId(projectValue);
        }
        List<Listener> listeners = gateway.getSpec().getListeners();
        if (!CollectionUtils.isEmpty(listeners)){
            //目前只支持一个listener
            Listener listener = listeners.get(0);
            gatewayDTO.setProtocol(listener.getProtocol());
            gatewayDTO.setHost(listener.getHostname());
            gatewayDTO.setPort(listener.getPort());
        }
        List<HTTPRoute> httpRoutes = getHTTPRoute(metadata.getName());
        if (!CollectionUtils.isEmpty(httpRoutes)){
            List<String> httpHosts = httpRoutes.stream().map(o -> o.getSpec().getHostnames()).flatMap(Collection::stream).distinct().collect(Collectors.toList());
            gatewayDTO.setRouteHosts(httpHosts);
        }
        gatewayDTO.setContent(getGatewayContent(gateway));
        return gatewayDTO;
    }

    private String getGatewayContent(Gateway gateway){
        Gateway targetGateway = new Gateway();
        targetGateway.setApiVersion(gateway.getApiVersion());
        targetGateway.setKind(gateway.getKind());
        ObjectMeta targetMetadata = new ObjectMeta();
        targetMetadata.setName(gateway.getMetadata().getName());
        targetMetadata.setNamespace(gateway.getMetadata().getNamespace());
        targetGateway.setMetadata(targetMetadata);
        targetGateway.setSpec(gateway.getSpec());
        return obj2yaml(targetGateway);
    }

    private HTTPRoute convertHTTPRoute(HasMetadata hasMetadata){
        if (hasMetadata instanceof HTTPRoute){
            HTTPRoute httpRoute = (HTTPRoute) hasMetadata;
            HTTPRoute target = new HTTPRoute();
            target.setApiVersion(httpRoute.getApiVersion());
            target.setKind(httpRoute.getKind());
            ObjectMeta targetMetadata = new ObjectMeta();
            targetMetadata.setName(httpRoute.getMetadata().getName());
            targetMetadata.setNamespace(httpRoute.getMetadata().getNamespace());
            target.setMetadata(targetMetadata);
            target.setSpec(httpRoute.getSpec());
            return target;
        }
        return null;
    }


    private String obj2yaml(HasMetadata hasMetadata){
        try {
            return new YAMLMapper().writeValueAsString(hasMetadata);
        }catch (Exception e){
            log.error("obj2yaml error, name:{}", hasMetadata.getMetadata().getName());
        }
        return Strings.EMPTY;
    }

}
