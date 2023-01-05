package org.hango.cloud.util.constant;

/**
 *
 * @date 2021/12/21
 */
public class PluginConstant {
    /**
     * 插件未初始化完毕的默认名称
     */
    public static final String DEFAULT_PLUGIN_NAME = "default-plugin-name";
    /**
     * 插件中设置网关出错时使用的默认网关值
     */
    public static final String DEFAULT_GATEWAY_NAME = "default-gateway-name";
    /**
     * 插件默认服务名（轻舟平台缩写）
     */
    public static final String DEFAULT_SERVICE_NAME = "qz";
    /**
     * 默认用户ID（默认为空）
     */
    public static final String DEFAULT_USER_ID = "";

    /**
     * 集群限流插件名
     */
    public static final String CLUSTER_LIMITER = "rate-limiting";
    /**
     * 本地限流插件名
     */
    public static final String LOCAL_LIMITER = "local-limiting";
}
