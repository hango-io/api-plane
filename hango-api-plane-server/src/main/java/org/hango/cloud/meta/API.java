package org.hango.cloud.meta;


import java.util.List;
import java.util.Map;


public class API extends CommonModel {

    private List<String> gateways;

    /**
     * api名
     */
    private String name;

    private List<String> hosts;

    /**
     * 请求uri
     */
    private List<String> requestUris;

    /**
     * 请求uri匹配方式, regex,prefix,exact
     */
    private UriMatch uriMatch;

    /**
     * 请求方法,GET、POST...
     */
    private List<String> methods;

    /**
     * 请求headers
     */
    private List<PairMatch> headers;

    /**
     * 请求参数
     */
    private List<PairMatch> queryParams;

    /**
     * 映射到后端的uri
     */
    private List<String> proxyUris;

    private List<Service> proxyServices;
    /**
     * 服务名
     */
    private String service = "qz";

    /**
     * 插件
     */
    private List<String> plugins;

    private Boolean extractMethod;
    /**
     * 负载均衡
     */
    private String loadBalancer;

    /**
     * 请求是否幂等
     */
    private Boolean Idempotent;

    /**
     * 保留原始host
     */
    private Boolean preserveHost;

    /**
     * 重试次数
     */
    private Integer retries;

    private Long connectTimeout;

    /**
     * 上下游超时时间(发送、读取)
     */
    private Long IdleTimeout;

    private Boolean httpsOnly;

    private Boolean httpIfTerminated;

    /**
     * 协议，默认HTTP
     */
    private String protocol = "HTTP";

    /**
     * 网关暴露端口，默认80
     */
    private Integer port = 80;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 服务标志
     */
    private String serviceTag;

    /**
     * api id
     */
    private Long apiId;

    /**
     * api name
     */
    private String apiName;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 超时时间
     */
    private Long timeout;

    /**
     * 重试次数
     */
    private int attempts;

    /**
     * 重试超时时间
     */
    private long perTryTimeout;

    /**
     * 重试条件
     */
    private String retryOn;

    /**
     * request header操作
     */
    private RequestOperation requestOperation;

    /**
     * 流量镜像配置
     */
    private Service mirrorTraffic;

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
    private Map<String, String> metaMap;

    private Long version;


    private int customDefaultRespCode;

    public Service getMirrorTraffic() {
        return mirrorTraffic;
    }

    public void setMirrorTraffic(Service mirrorTraffic) {
        this.mirrorTraffic = mirrorTraffic;
    }

    public List<String> getGateways() {
        return gateways;
    }

    public void setGateways(List<String> gateways) {
        this.gateways = gateways;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<String> getProxyUris() {
        return proxyUris;
    }

    public void setProxyUris(List<String> proxyUris) {
        this.proxyUris = proxyUris;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public Boolean getExtractMethod() {
        return extractMethod;
    }

    public void setExtractMethod(Boolean extractMethod) {
        this.extractMethod = extractMethod;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Boolean getIdempotent() {
        return Idempotent;
    }

    public void setIdempotent(Boolean idempotent) {
        Idempotent = idempotent;
    }

    public Boolean getPreserveHost() {
        return preserveHost;
    }

    public void setPreserveHost(Boolean preserveHost) {
        this.preserveHost = preserveHost;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getIdleTimeout() {
        return IdleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        IdleTimeout = idleTimeout;
    }

    public Boolean getHttpsOnly() {
        return httpsOnly;
    }

    public void setHttpsOnly(Boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }

    public Boolean getHttpIfTerminated() {
        return httpIfTerminated;
    }

    public void setHttpIfTerminated(Boolean httpIfTerminated) {
        this.httpIfTerminated = httpIfTerminated;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public UriMatch getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(UriMatch uriMatch) {
        this.uriMatch = uriMatch;
    }

    public List<Service> getProxyServices() {
        return proxyServices;
    }

    public void setProxyServices(List<Service> proxyServices) {
        this.proxyServices = proxyServices;
    }

    public List<PairMatch> getHeaders() {
        return headers;
    }

    public void setHeaders(List<PairMatch> headers) {
        this.headers = headers;
    }

    public List<PairMatch> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<PairMatch> queryParams) {
        this.queryParams = queryParams;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
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

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }


    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public long getPerTryTimeout() {
        return perTryTimeout;
    }

    public void setPerTryTimeout(long perTryTimeout) {
        this.perTryTimeout = perTryTimeout;
    }

    public String getRetryOn() {
        return retryOn;
    }

    public void setRetryOn(String retryOn) {
        this.retryOn = retryOn;
    }

    public RequestOperation getRequestOperation() {
        return requestOperation;
    }

    public void setRequestOperation(RequestOperation requestOperation) {
        this.requestOperation = requestOperation;
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

    public int getCustomDefaultRespCode() {
        return customDefaultRespCode;
    }

    public void setCustomDefaultRespCode(int customDefaultRespCode) {
        this.customDefaultRespCode = customDefaultRespCode;
    }
}
