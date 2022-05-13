package org.hango.cloud.service.impl;

import com.jayway.jsonpath.Criteria;
import org.hango.cloud.core.editor.EditorContext;
import org.hango.cloud.core.editor.PathExpressionEnum;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.core.template.TemplateTranslator;
import org.hango.cloud.meta.WhiteList;
import org.hango.cloud.service.WhiteListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.hango.cloud.core.k8s.K8sResourceEnum.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
@Service
public class WhiteListServiceImpl implements WhiteListService {
    private static final Logger logger = LoggerFactory.getLogger(WhiteListServiceImpl.class);


    @Autowired
    private KubernetesClient client;

    @Autowired
    private TemplateTranslator templateTranslator;

    @Autowired
    private EditorContext editorContext;

    private static final String RBAC_INGRESS_TEMPLATE_NAME = "rbac_ingress";
    private static final String WHITELIST_TEMPLATE_NAME = "whiteList";
    private static final String YAML_SPLIT = "---";

    /**
     * 要求每次都上报全量的values
     *
     * @param whiteList
     */
    @Override
    public void updateService(WhiteList whiteList) {
        String[] yamls = templateTranslator.translate(WHITELIST_TEMPLATE_NAME, whiteList, YAML_SPLIT);
        for (String yaml : yamls) {
            if (!yaml.contains("apiVersion")) {
                continue;
            }
            client.createOrUpdate(yaml, ResourceType.YAML);
        }
    }

    @Override
    public void removeService(WhiteList whiteList) {
        String role = client.get(ServiceRole.name(), whiteList.getNamespace(), "qz-ingress-whitelist");
        ResourceGenerator generator = ResourceGenerator.newInstance(role, ResourceType.OBJECT, editorContext);
        generator.removeElement(PathExpressionEnum.REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getService()));
        client.createOrUpdate(generator.jsonString(), ResourceType.JSON);

        String service = whiteList.getService();
        String namespace = whiteList.getNamespace();

        // todo: 可能会误删
        client.delete(VirtualService.name(), namespace, getVirtualServiceName(service));
        client.delete(DestinationRule.name(), namespace, getDestinationRuleName(service));
        client.delete(ServiceRole.name(), namespace, getServiceRoleName(service));
        client.delete(ServiceRoleBinding.name(), namespace, getServiceRoleBindingName(service));
        client.delete(Policy.name(), namespace, getPolicyName(service));
    }

	private String getVirtualServiceName(String service) {
        return String.format("%s", service);
    }

    private String getDestinationRuleName(String service) {
        return String.format("%s", service);
    }

    private String getServiceRoleName(String service) {
        return String.format("%s", service);
    }

    private String getServiceRoleBindingName(String service) {
        return String.format("%s", service);
    }

    private String getPolicyName(String service) {
        return String.format("%s", service);
    }
}
