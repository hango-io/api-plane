package org.hango.cloud.service.impl;

import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.KubernetesClient;
import org.hango.cloud.meta.dto.WhiteListV2AuthRuleDto;
import org.hango.cloud.service.WhiteListV2Service;
import org.hango.cloud.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import me.snowdrop.istio.api.rbac.v1alpha1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @auther wengyanghui@corp.netease.com
 * @date 2020/4/10
 **/
@Service
public class WhiteListV2ServiceImpl implements WhiteListV2Service {

    @Autowired
    private KubernetesClient k8sClient;

    @Override
    public void updateServiceAuth(String service, Boolean rbacTurnOn, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList) {

        K8sService k8sService = new K8sService(service);
        RbacConfig existRbacConfig = k8sClient.getObject(K8sResourceEnum.ClusterRbacConfig.name(), k8sService.namespace, "default");
        String serviceFQDN = k8sService.getFQDN();

        if (existRbacConfig == null) {
            // crate new rbac config
            Target target = new Target();
            if (rbacTurnOn) {
                target.setServices(Arrays.asList(serviceFQDN));
            }
            RbacConfigBuilder rbacConfigBuilder = new RbacConfigBuilder();
            RbacConfig newRbacConfig = rbacConfigBuilder
                    .withKind("ClusterRbacConfig")
                    .withMetadata(new ObjectMetaBuilder().withName("default").build())
                    .withSpec(new RbacConfigSpecBuilder().withMode(Mode.ON_WITH_INCLUSION).withInclusion(target).build())
                    .build();
            k8sClient.createOrUpdate(newRbacConfig, ResourceType.OBJECT);
        } else {
            // update exist rbac config
            List<String> existServices = existRbacConfig.getSpec().getInclusion().getServices();
            if (rbacTurnOn) {
                if (!existServices.contains(serviceFQDN)) {
                    existServices.add(serviceFQDN);
                }
            } else {
                if (existServices.contains(serviceFQDN)) {
                    existServices.remove(serviceFQDN);
                }
            }
            k8sClient.createOrUpdate(existRbacConfig, ResourceType.OBJECT);
        }

        updateServiceRoleOthersConfigsByPolicy(k8sService, defaultPolicy, authRuleList);
    }

    @Override
    public void createOrUpdateAuthRule(String service, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList) {
        K8sService k8sService = new K8sService(service);

        // 先分别创建 ServiceRole 和 ServiceRoleBinding
        for (WhiteListV2AuthRuleDto rule : authRuleList) {

            if(rule.getEnabled()){
                // 创建或者修改配置
                createOrUpdateRuleConfig(k8sService, rule);
            } else {
                // 删除存在的配置
                deleteIfExistRuleConfig(k8sService, rule);
            }

        }

        updateServiceRoleOthersConfigsByPolicy(k8sService, defaultPolicy, authRuleList);
    }

    private void createOrUpdateRuleConfig(K8sService k8sService, WhiteListV2AuthRuleDto rule) {
        String serviceRoleConfigName = k8sService.getCrdCompName() + "-" + rule.getRuleName();
        // service role config
        ServiceRole serviceRoleConfig = k8sClient.getObject(K8sResourceEnum.ServiceRole.name(), k8sService.namespace, serviceRoleConfigName);
        AccessRule accessRule = MatchApi.parseToAccessRule(k8sService.getFQDN(), rule.getMatchApis());
        if (serviceRoleConfig == null) {
            ServiceRoleBuilder serviceRoleBuilder = new ServiceRoleBuilder();
            serviceRoleConfig = serviceRoleBuilder
                    .withKind("ServiceRole")
                    .withMetadata(new ObjectMetaBuilder().withNamespace(k8sService.namespace).withName(serviceRoleConfigName).build())
                    .withSpec(new ServiceRoleSpecBuilder().addToRules(accessRule).build())
                    .build();
        } else {
            serviceRoleConfig.getSpec().getRules().clear();
            serviceRoleConfig.getSpec().getRules().add(accessRule);
        }
        k8sClient.createOrUpdate(serviceRoleConfig, ResourceType.OBJECT);

        // service role binding config
        ServiceRoleBinding serviceRoleBindingConfig = k8sClient.getObject(K8sResourceEnum.ServiceRoleBinding.name(), k8sService.namespace, serviceRoleConfigName);
        List<Subject> subjectList = AbsMatchCondition.parseToSubjects(rule.getMatchType(), rule.getMatchConditions());
        if (serviceRoleBindingConfig == null) {
            ServiceRoleBindingBuilder serviceRoleBindingBuilder = new ServiceRoleBindingBuilder();
            RoleRef roleRef = new RoleRefBuilder().withKind("ServiceRole").withName(serviceRoleConfigName).build();
            serviceRoleBindingConfig = serviceRoleBindingBuilder
                    .withKind("ServiceRoleBinding")
                    .withMetadata(new ObjectMetaBuilder().withNamespace(k8sService.namespace).withName(serviceRoleConfigName).build())
                    .withSpec(new ServiceRoleBindingSpecBuilder().withRoleRef(roleRef).addAllToSubjects(subjectList).build())
                    .build();
        } else {
            serviceRoleBindingConfig.getSpec().getSubjects().clear();
            serviceRoleBindingConfig.getSpec().getSubjects().addAll(subjectList);
        }
        k8sClient.createOrUpdate(serviceRoleBindingConfig, ResourceType.OBJECT);
    }

    private void deleteIfExistRuleConfig(K8sService k8sService, WhiteListV2AuthRuleDto rule) {
        String serviceRoleConfigName = k8sService.getCrdCompName() + "-" + rule.getRuleName();
        ServiceRole serviceRoleConfig = k8sClient.getObject(K8sResourceEnum.ServiceRole.name(), k8sService.namespace, serviceRoleConfigName);
        if(serviceRoleConfig != null){
            k8sClient.delete(K8sResourceEnum.ServiceRole.name(), k8sService.namespace, serviceRoleConfigName);
        }
        ServiceRoleBinding serviceRoleBindingConfig = k8sClient.getObject(K8sResourceEnum.ServiceRoleBinding.name(), k8sService.namespace, serviceRoleConfigName);
        if(serviceRoleBindingConfig != null){
            k8sClient.delete(K8sResourceEnum.ServiceRoleBinding.name(), k8sService.namespace, serviceRoleConfigName);
        }
    }

    private void updateServiceRoleOthersConfigsByPolicy(K8sService k8sService, String defaultPolicy, List<WhiteListV2AuthRuleDto> allRuleList) {

        String serviceRoleOtherConfigName = k8sService.getCrdCompName() + "-others";

        if ("deny".equalsIgnoreCase(defaultPolicy)) {
            // 删除所有对应的others信息
            ServiceRole serviceRoleConfigOthers = k8sClient.getObject(K8sResourceEnum.ServiceRole.name(), k8sService.namespace, serviceRoleOtherConfigName);
            if (serviceRoleConfigOthers != null) {
                k8sClient.delete(K8sResourceEnum.ServiceRole.name(), k8sService.namespace, serviceRoleOtherConfigName);
            }

            ServiceRoleBinding serviceRoleBindingOthersConfig = k8sClient.getObject(K8sResourceEnum.ServiceRoleBinding.name(), k8sService.namespace, serviceRoleOtherConfigName);
            if (serviceRoleBindingOthersConfig != null) {
                k8sClient.delete(K8sResourceEnum.ServiceRoleBinding.name(), k8sService.namespace, serviceRoleOtherConfigName);
            }
        } else if ("allow".equalsIgnoreCase(defaultPolicy)) {
            HashSet<String> allApisSet = new HashSet<>();
            for (WhiteListV2AuthRuleDto rule : allRuleList) {
                //  add api to set for "others" config
                if(rule.getEnabled()){
                    MatchApi.parseList(rule.getMatchApis()).forEach(api -> allApisSet.add(api.getAccessRulePath()));
                }
            }

            // service role other configs
            ServiceRole serviceRoleConfigOthers = k8sClient.getObject(K8sResourceEnum.ServiceRole.name(), k8sService.namespace, serviceRoleOtherConfigName);
            AccessRule accessRuleOthers = new AccessRule();
            accessRuleOthers.setNotPaths(new ArrayList<>(allApisSet));
            accessRuleOthers.setServices(Arrays.asList(k8sService.getFQDN()));

            if (serviceRoleConfigOthers == null) {
                ServiceRoleBuilder serviceRoleBuilder = new ServiceRoleBuilder();
                serviceRoleConfigOthers = serviceRoleBuilder
                        .withKind("ServiceRole")
                        .withMetadata(new ObjectMetaBuilder().withNamespace(k8sService.namespace).withName(serviceRoleOtherConfigName).build())
                        .withSpec(new ServiceRoleSpecBuilder().addToRules(accessRuleOthers).build())
                        .build();
            } else {
                serviceRoleConfigOthers.getSpec().getRules().clear();
                serviceRoleConfigOthers.getSpec().getRules().add(accessRuleOthers);
            }
            k8sClient.createOrUpdate(serviceRoleConfigOthers, ResourceType.OBJECT);

            // service role binding other configs
            ServiceRoleBinding serviceRoleBindingOthersConfig = k8sClient.getObject(K8sResourceEnum.ServiceRoleBinding.name(), k8sService.namespace, serviceRoleOtherConfigName);
            List<Subject> subjectOthersList = new ArrayList<>();
            subjectOthersList.add(new SubjectBuilder().withUser("*").build());
            if (serviceRoleBindingOthersConfig == null) {
                ServiceRoleBindingBuilder serviceRoleBindingBuilder = new ServiceRoleBindingBuilder();
                RoleRef roleRef = new RoleRefBuilder().withKind("ServiceRole").withName(serviceRoleOtherConfigName).build();
                serviceRoleBindingOthersConfig = serviceRoleBindingBuilder
                        .withKind("ServiceRoleBinding")
                        .withMetadata(new ObjectMetaBuilder().withNamespace(k8sService.namespace).withName(serviceRoleOtherConfigName).build())
                        .withSpec(new ServiceRoleBindingSpecBuilder().withRoleRef(roleRef).addAllToSubjects(subjectOthersList).build())
                        .build();
            } else {
                serviceRoleBindingOthersConfig.getSpec().getSubjects().clear();
                serviceRoleBindingOthersConfig.getSpec().getSubjects().addAll(subjectOthersList);
            }
            k8sClient.createOrUpdate(serviceRoleBindingOthersConfig, ResourceType.OBJECT);

        } else {
            throw new ApiPlaneException("Unsupported Default Policy: " + defaultPolicy);
        }
    }

    @Override
    public void deleteAuthRule(String service, String ruleName, String defaultPolicy, List<WhiteListV2AuthRuleDto> authRuleList) {

        K8sService k8sService = new K8sService(service);
        String serviceRoleConfigName = k8sService.getCrdCompName() + "-" + ruleName;

        if (k8sClient.getObject("ServiceRole", k8sService.getNamespace(), serviceRoleConfigName) != null) {
            k8sClient.delete("ServiceRole", k8sService.getNamespace(), serviceRoleConfigName);
        }

        if (k8sClient.get("ServiceRoleBinding", k8sService.getNamespace(), serviceRoleConfigName) != null) {
            k8sClient.delete("ServiceRoleBinding", k8sService.getNamespace(), serviceRoleConfigName);
        }

        updateServiceRoleOthersConfigsByPolicy(k8sService, defaultPolicy, authRuleList);
    }

    private class K8sService {
        private String service;
        private String namespace;

        public K8sService(String fullServiceName) {
            if (fullServiceName.contains(".")) {
                String[] strs = fullServiceName.split("\\.");
                this.service = strs[0];
                this.namespace = strs[1];
            } else {
                this.service = fullServiceName;
                this.namespace = "default";
            }
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getFullName() {
            return this.service + "." + this.namespace;
        }

        public String getFQDN() {
            // todo: 多集群适配
            return this.service + "." + this.namespace + ".svc.cluster.local";
        }

        public String getCrdCompName() {
            return this.service + "-" + this.namespace;
        }
    }

    private static class MatchApi {

        /**
         * 匹配类型，目前暂不用解析直接透传给crd
         * 空： 默认全字符串匹配
         * regex: 正则匹配
         */
        private String matchType;
        /**
         * api 纯路径部分
         */
        private String path;

        /**
         * 预留的api 方法字段，目前还不支持单独api级别配置
         */
        private String method;

        /**
         * 透传给crd的api地址
         */
        private String accessRulePath;

        public MatchApi(String apiStr) {
            if (apiStr.contains(":")) {
                String[] parts = apiStr.split("\\:");
                this.matchType = parts[0];
                this.path = parts[1];
            } else {
                this.matchType = "";
                this.path = apiStr;
            }
            this.accessRulePath = apiStr;
        }

        public static List<MatchApi> parseList(String matchApis) {
            List<MatchApi> resApis = new ArrayList<>();
            String[] apiParts = matchApis.split("\\;");
            for (String api : apiParts) {
                resApis.add(new MatchApi(api));
            }
            return resApis;
        }

        public static AccessRule parseToAccessRule(String svcFQDN, String matchApis) {
            List<MatchApi> apiList = parseList(matchApis);
            AccessRule accessRule = new AccessRule();
            apiList.forEach(api -> accessRule.getPaths().add(api.getAccessRulePath()));
            accessRule.setServices(Arrays.asList(svcFQDN));
            return accessRule;
        }

        public String getAccessRulePath() {
            return accessRulePath;
        }

        public void setAccessRulePath(String accessRulePath) {
            this.accessRulePath = accessRulePath;
        }
    }

    private abstract static class AbsMatchCondition {

        public static List<Subject> parseToSubjects(String matchType, String matchConditions) {
            if ("service".equalsIgnoreCase(matchType)) {
                return MatchConditionService.parseSubjects(matchConditions);
            } else if ("header".equalsIgnoreCase(matchType)) {
                return MatchConditionHeader.parseSubjects(matchConditions);
            } else {
                // return empty if not match any type
                return new ArrayList<>();
            }
        }
    }

    private static class MatchConditionService extends AbsMatchCondition {

        private String service;
        private String namespace;

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public MatchConditionService(String matchConditionStr) {
            if (matchConditionStr.contains(".")) {
                String[] parts = matchConditionStr.split("\\.");
                this.service = parts[0];
                this.namespace = parts[1];
            } else {
                this.service = matchConditionStr;
                this.namespace = "default";
            }
        }

        public static List<MatchConditionService> parseConditions(String matchConditions) {
            List<MatchConditionService> resConditions = new ArrayList<>();
            String[] conditionsParts = matchConditions.split("\\;");
            for (String api : conditionsParts) {
                resConditions.add(new MatchConditionService(api));
            }
            return resConditions;
        }

        public static List<Subject> parseSubjects(String matchConditions) {
            List<MatchConditionService> matchConditionServiceList = parseConditions(matchConditions);
            List<Subject> subjects = new ArrayList<>();
            for (MatchConditionService service : matchConditionServiceList) {
//                subjects.add(new SubjectBuilder().withUser("cluster.local/ns/" + service.getNamespace() +
//                        "/sa/" + service.getService()).build());
                // todo: app对应的service 不一定是唯一
                HashMap<String, String> map = new HashMap<>();
                map.put("request.headers[" + "x-nsf-app" + "]", service.getService() + "." + service.getNamespace());
                subjects.add(new SubjectBuilder().withProperties(map).build());
            }
            return subjects;
        }

    }

    private static class MatchConditionHeader extends AbsMatchCondition {

        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public MatchConditionHeader(String matchConditionStr) {
            if (matchConditionStr.contains("：")) {
                String[] parts = matchConditionStr.split("\\:");
                this.key = parts[0];
                this.value = parts[1];
            } else {
                this.key = "*";
                this.value = matchConditionStr;
            }
        }

        public static List<MatchConditionHeader> parseConditions(String matchConditions) {
            List<MatchConditionHeader> resConditions = new ArrayList<>();
            String[] conditionsParts = matchConditions.split("\\;");
            for (String api : conditionsParts) {
                resConditions.add(new MatchConditionHeader(api));
            }
            return resConditions;
        }

        public static List<Subject> parseSubjects(String matchConditions) {
            List<MatchConditionHeader> matchConditionServiceList = parseConditions(matchConditions);
            List<Subject> subjects = new ArrayList<>();
            for (MatchConditionHeader header : matchConditionServiceList) {
                HashMap<String, String> map = new HashMap<>();
                map.put("request.headers[" + header.getKey() + "]", header.getValue());
                subjects.add(new SubjectBuilder().withProperties(map).build());
            }
            return subjects;
        }
    }
}
