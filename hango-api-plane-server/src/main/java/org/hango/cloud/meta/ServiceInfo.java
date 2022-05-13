package org.hango.cloud.meta;

import org.springframework.util.StringUtils;

import static org.hango.cloud.core.template.TemplateConst.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/12
 **/
public class ServiceInfo {
    // 提供apiName占位符，例如${t_api_name},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得apiName
    private String apiName = wrap(SERVICE_INFO_API_NAME);

    private String serviceName = wrap(SERVICE_INFO_API_SERVICE);

    private String gateway = wrap(SERVICE_INFO_API_GATEWAY);

    // 提供method占位符，例如${t_api_methods},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得method
    private String method = wrap(SERVICE_INFO_API_METHODS);

    // 提供uri占位符，例如${t_api_request_uris},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得uri
    private String uri = wrap(SERVICE_INFO_API_REQUEST_URIS);

    // 提供subset占位符，例如${t_virtual_service_subset_name},后续GatewayModelProcessor会进行渲染，不能从API中获得
    private String subset = wrap(SERVICE_INFO_VIRTUAL_SERVICE_SUBSET_NAME);

    private String hosts = wrap(SERVICE_INFO_VIRTUAL_SERVICE_HOST_HEADERS);

    private String priority = wrap(SERVICE_INFO_VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY);

    private String matchYaml;

    public static ServiceInfo instance() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setPriority(null);
        serviceInfo.setApiName(null);
        serviceInfo.setGateway(null);
        serviceInfo.setHosts(null);
        serviceInfo.setServiceName(null);
        serviceInfo.setSubset(null);
        serviceInfo.setUri(null);
        serviceInfo.setMethod(null);
        return serviceInfo;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMatchYaml() {
        return matchYaml;
    }

    public void setMatchYaml(String matchYaml) {
        this.matchYaml = matchYaml;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "apiName='" + apiName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", gateway='" + gateway + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", subset='" + subset + '\'' +
                ", hosts='" + hosts + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }

    private String wrap(String raw) {
        if (StringUtils.isEmpty(raw)) throw new NullPointerException();
        return "${" + raw + "}";
    }
}
