package org.hango.cloud.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.core.plugin.PluginGenerator;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.hango.cloud.util.Const.*;


public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static YAMLMapper yamlMapper;
    /**
     * match ip:port
     * 127.0.0.1:8080
     */
    private static final Pattern IP_PORT_PATTERN =
            Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):(6553[0-5]|655[0-2][0-9]|65[0-4][0-9][0-9]|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})$");

    /**
     *
     * @param ipAddr ip:port
     * @return
     */
    public static boolean isValidIPPortAddr(String ipAddr) {
        return IP_PORT_PATTERN.matcher(ipAddr).matches();
    }

    /**
     * host变为正则
     *
     * . -> \.
     * * -> .+
     *
     * *.163.com -> .+\.163\.com
     * @return
     */
    public static String host2Regex(String host) {
        if (host.equals("*")) return ".*";
        return host.replace(".", "\\.")
                    .replace("*", ".+");
    }

    public static String obj2yaml(Object o) {

        if (o == null) return null;
        try {
            return getYamlMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.warn("obj {} to yaml failed", o, e);
        }
        return null;
    }

    public static <T> T yaml2Obj(String yaml, Class<T> clazz) {

        if (StringUtils.isEmpty(yaml)) return null;
        try {
            return getYamlMapper().readValue(yaml, clazz);
        } catch (IOException e) {
            logger.warn("yaml {} to obj failed,", yaml, e);
            throw new ApiPlaneException("yaml to obj failed");
        }
    }

    private static YAMLMapper getYamlMapper() {
        if (yamlMapper == null) {
            YAMLMapper mapper = new YAMLMapper();
            mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
            mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            yamlMapper = mapper;
        }
        return yamlMapper;
    }



    /**
     * 返回第n次出现的字符串的索引
     *
     * e.g.
     * str = outbound|9901|subset1|istio-galley.istio-system.svc.cluster.local
     * occur = |
     *
     * order = 0
     * return 8
     *
     * order = 1
     * return 13
     *
     * order = 2
     * return 21
     *
     * @param str
     * @param occur
     * @param order
     * @return
     */
    public static int xIndexOf(String str, String occur, int order) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(occur) || order < 0) return -1;
        if (order == 0) return str.indexOf(occur);
        return str.indexOf(occur, xIndexOf(str, occur, order-1) + 1);
    }


    /**
     * 获取插件名称，对于自定义插件，返回rider||wasm
     * @param rg
     * @return
     */
    public static String getPluginName(PluginGenerator rg){
        String kind = rg.getValue("$.kind", String.class);
        String pluginType = getPluginType(rg);
        return INLINE.equals(pluginType) ? kind : pluginType;
    }



    /**
     * 获取插件名称，对于自定义插件，返回插件名称，例如 fault-injection
     */
    public static String getKind(PluginGenerator rg){
        return rg.getValue("$.kind", String.class);
    }

    /**
     * 获取插件类型 rider||wasm||inline
     * @param rg
     * @return
     */
    public static String getPluginType(PluginGenerator rg){
        String type = rg.getValue("$.type", String.class);
        if (StringUtils.isBlank(type)) {
            return INLINE;
        }
        switch (type){
            case LUA :
            case RIDER:
                return RIDER;
            case WASM:
                return WASM;
            default:
                return INLINE;
        }
    }


}
