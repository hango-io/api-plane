package org.hango.cloud.meta;

/**
 * @Author zhufengwei
 * @Date 2023/7/31
 */
public enum PluginScopeTypeEnum {
    ROUTE("routeRule", "路由级插件"),
    HOST("host", "域名级插件"),
    //历史原因，global并不是全局插件，而是项目级插件
    GLOBAL("global", "项目级插件"),
    GATEWAY("gateway", "网关级插件")
    ;

    private final String value;

    private final String desc;

    PluginScopeTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public String getValue() {
        return value;
    }

    public static boolean isRoutePlugin(String value){
        return ROUTE.getValue().equals(value);
    }


    public static boolean isHostPlugin(String value){
        return HOST.getValue().equals(value) || GLOBAL.getValue().equals(value);
    }

    public static boolean isGatewayPlugin(String value){
        return GATEWAY.getValue().equals(value);
    }
}
