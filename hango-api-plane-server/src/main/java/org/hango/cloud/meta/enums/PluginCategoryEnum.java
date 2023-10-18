package org.hango.cloud.meta.enums;

import java.util.stream.Stream;

/**
 * @Author zhufengwei
 * @Date 2023/9/25
 */
public enum PluginCategoryEnum {
    PRE_BUILTIN("builtin", "系统前置插件", 0),
    SECURITY("security", "安全", 1),
    AUTH("auth", "认证", 2),
    TRAFFIC_POLICY("trafficPolicy", "流量管理", 3),
    DATA_FORMAT("dataFormat", "数据转换", 4),
    POST_BUILTIN("postBuiltin", "系统后置插件", 99);


    private final String category;
    private final String description;
    //插件执行顺序，安全类插件最先执行，数据转换类插件最后执行
    private final Integer order;

    PluginCategoryEnum(String category, String description, Integer order) {
        this.category = category;
        this.description = description;
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public static PluginCategoryEnum getPluginCategory(String category) {
        return Stream.of(values()).filter(pluginCategoryEnum -> pluginCategoryEnum.getCategory().equals(category)).findFirst().orElse(null);
    }

    public static PluginCategoryEnum getPluginCategory(Integer order) {
        return Stream.of(values()).filter(pluginCategoryEnum -> pluginCategoryEnum.getOrder().equals(order)).findFirst().orElse(null);
    }

    public static PluginCategoryEnum getNextCategory(String category) {
        PluginCategoryEnum pluginCategory = getPluginCategory(category);
        if (pluginCategory == null) {
            return null;
        }
        return getPluginCategory(pluginCategory.getOrder() + 1);
    }
}
