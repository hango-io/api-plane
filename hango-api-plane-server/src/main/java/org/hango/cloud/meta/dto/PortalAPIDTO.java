package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hango.cloud.meta.CRDMetaEnum;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class PortalAPIDTO {

    @NotEmpty(message = "gateway")
    @JsonProperty(value = "Gateway")
    private String gateway;

    /**
     * api唯一标识
     */
    @NotEmpty(message = "api code")
    @JsonProperty(value = "Code")
    private String code;

    @NotEmpty(message = "hosts")
    @JsonProperty(value = "Hosts")
    private List<String> hosts;

    /**
     * 请求uri
     */
    @NotEmpty(message = "request uris")
    @JsonProperty(value = "RequestUris")
    private List<String> requestUris;

    /**
     * 请求uri匹配方式
     */
    @NotEmpty(message = "uri match")
    @JsonProperty(value = "UriMatch")
    private String uriMatch;

    /**
     * 请求方法,GET、POST...
     */
    @JsonProperty(value = "Methods")
    private List<String> methods;

    /**
     * 映射到后端的uri
     */
    @Valid
    @JsonProperty(value = "ProxyServices")
    @NotNull(message = "proxyServices")
    private List<PortalRouteServiceDTO> proxyServices;

    /**
     * 插件
     */
    @JsonProperty(value = "Plugins")
    private List<String> plugins;

    /**
     * 请求头
     */
    @JsonProperty(value = "Headers")
    @Valid
    private List<PairMatchDTO> headers;

    @JsonProperty(value = "Order")
    private Integer priority;
    /**
     * 请求的query params
     */
    @JsonProperty(value = "QueryParams")
    @Valid
    private List<PairMatchDTO> queryParams;

    /**
     * 路由名字
     */
    @JsonProperty(value = "RouteName")
    @NotNull(message = "RouteName")
    private String routeName;

    /**
     * 路由超时时间
     */
    @JsonProperty(value = "Timeout")
    private Long timeout;

    /**
     * 路由重试策略
     */
    @JsonProperty(value = "HttpRetry")
    private HttpRetryDTO httpRetry;

    /**
     * 租户ID
     */
    @JsonProperty(value = "TenantId")
    private String tenantId;

    /**
     * 项目ID
     */
    @JsonProperty(value = "ProjectId")
    @NotNull(message = "ProjectId")
    private String projectId;

    @JsonProperty(value = "RequestOperation")
    @Valid
    private RequestOperationDTO requestOperation;

    /**
     * VirtualCluster配置
     */
    @JsonProperty(value = "VirtualCluster")
    @Valid
    private VirtualClusterDTO virtualClusterDTO;

    /**
     * 流量镜像配置
     */
    @JsonProperty(value = "MirrorTraffic")
    private PortalMirrorTrafficDto mirrorTrafficDto;

    /**
     * meta数据传输集
     * Map<mata_type,meta_data>
     * mata_type meta类型
     *
     * @see CRDMetaEnum
     * mata_type: 路由meta数据类型
     * meta_data: 路由meta数据值，使用JSON传输
     * eg.
     * {
     * "DubboMeta": {
     * "ObjectType": "route",
     * "ObjectId": 259,
     * "Params": [
     * {
     * "Key": "str",
     * "Value": "java.lang.String",
     * "GenericInfo": ".dataA:com.demo.B,.dataAA:com.demo.B,.dataA.dataB:com.demo.C,.dataAA.dataB:com.demo.C",
     * "Required": false,
     * "DefaultValue": "sdfsdfs",
     * "_formTableKey": 1660649073793,
     * "index": 0
     * },
     * {
     * "Key": "wer",
     * "Value": "java.lang.Integer",
     * "GenericInfo": null,
     * "Required": true,
     * "DefaultValue": null,
     * "_formTableKey": 1660649073793
     * }
     * ],
     * "Method": "echoStrAndInt",
     * "CustomParamMapping": true,
     * "ParamSource": "body",
     * "Attachment": [
     * {
     * "ClientParamName": "xcvxcv",
     * "Description": "cxvxcv",
     * "ParamPosition": "Header",
     * "ServerParamName": "cvcv",
     * "distinctName": "Headerxcvxcv",
     * "_formTableKey": 1660648830195
     * }
     * ],
     * "MethodWorks": true
     * },
     * "StatsMeta": [
     * "/test",
     * "/test1"
     * ]
     * }
     */
    @JsonProperty(value = "MetaMap")
    private Map<String, String> metaMap;

    @JsonProperty(value = "Version")
    private Long version;

    public PortalMirrorTrafficDto getMirrorTrafficDto() {
        return mirrorTrafficDto;
    }

    public void setMirrorTrafficDto(PortalMirrorTrafficDto mirrorTrafficDto) {
        this.mirrorTrafficDto = mirrorTrafficDto;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getRequestUris() {
        return requestUris;
    }

    public void setRequestUris(List<String> requestUris) {
        this.requestUris = requestUris;
    }

    public String getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(String uriMatch) {
        this.uriMatch = uriMatch;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<PortalRouteServiceDTO> getProxyServices() {
        return proxyServices;
    }

    public void setProxyServices(List<PortalRouteServiceDTO> proxyServices) {
        this.proxyServices = proxyServices;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public List<PairMatchDTO> getHeaders() {
        return headers;
    }

    public void setHeaders(List<PairMatchDTO> headers) {
        this.headers = headers;
    }

    public List<PairMatchDTO> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<PairMatchDTO> queryParams) {
        this.queryParams = queryParams;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public HttpRetryDTO getHttpRetry() {
        return httpRetry;
    }

    public void setHttpRetry(HttpRetryDTO httpRetry) {
        this.httpRetry = httpRetry;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public RequestOperationDTO getRequestOperation() {
        return requestOperation;
    }

    public void setRequestOperation(RequestOperationDTO requestOperation) {
        this.requestOperation = requestOperation;
    }

    public VirtualClusterDTO getVirtualClusterDTO() {
        return virtualClusterDTO;
    }

    public void setVirtualClusterDTO(VirtualClusterDTO virtualClusterDTO) {
        this.virtualClusterDTO = virtualClusterDTO;
    }

    public Map<String, String> getMetaMap() {
        return metaMap;
    }

    public void setMetaMap(Map<String, String> metaMap) {
        this.metaMap = metaMap;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
