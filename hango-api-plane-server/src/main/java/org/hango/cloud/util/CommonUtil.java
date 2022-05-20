package org.hango.cloud.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.hango.cloud.core.editor.ResourceGenerator;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.K8sResourceGenerator;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.function.Equals;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;


public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static YAMLMapper yamlMapper;
    private static ObjectMapper objectMapper = new ObjectMapper();
    /**
     * match ip:port
     * 127.0.0.1:8080
     */
    private static final Pattern IP_PORT_PATTERN =
            Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):(6553[0-5]|655[0-2][0-9]|65[0-4][0-9][0-9]|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})$");

    public static Map<String, String> str2Label(String str) {

        Map<String, String> labelMap = new HashMap<>();
        if (StringUtils.isEmpty(str) || !str.contains(":")) return labelMap;
        String[] label = str.split(":");
        labelMap.put(label[0], label[1]);
        return labelMap;
    }

    public static Map<String, String> strs2Label(List<String> strs) {

        Map<String, String> labelMap = new HashMap<>();
        if (CollectionUtils.isEmpty(strs)) return labelMap;
        strs.forEach(s -> {
            labelMap.putAll(str2Label(s));
        });
        return labelMap;
    }

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
     * 合并两个list, 当遇到相等的两个element时，
     * 用新list中的element取代老list中的element，
     * 不相等的element全部保留。
     * @param oldL
     * @param newL
     * @param eq
     * @return
     */
     public static List mergeList(List oldL, List newL, Equals eq) {
        List result = null;
        if (!CollectionUtils.isEmpty(newL)) {
            if (CollectionUtils.isEmpty(oldL)) {
                return newL;
            } else {
                result = new ArrayList(oldL);
                for (Object no : newL) {
                    for (Object oo : oldL) {
                        if (eq.apply(no, oo)) {
                            result.remove(oo);
                        }
                    }
                }
                result.addAll(newL);
            }
        }
        return result;
    }

    public static List dropList(List oldL, Object identical, Equals eq) {
        if (CollectionUtils.isEmpty(oldL)) return oldL;
        List result = new ArrayList(oldL);
        for (Object oldO : oldL) {
            if (eq.apply(oldO, identical)) {
                result.remove(oldO);
            }
        }
        return result;
    }

    public static HasMetadata json2HasMetadata(String json) {
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(json, ResourceType.JSON);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        return gen.object(resourceEnum.mappingType());
    }

    public static boolean isLuaPlugin(String plugin) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        String type = source.getValue("$.type");
        String kind = source.getValue("$.kind");
        return "lua".equals(type) || "trace".equals(kind);
    }

    public static <T> T safelyGet(Supplier<T> getter) {
         try {
             return getter.get();
         } catch (NullPointerException npe) {
             return null;
         }
    }

    public static <T> Optional<T> safely(Supplier<T> getter) {
     	return Optional.ofNullable(safelyGet(getter));
    }

    /**
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T copy(T t) {

        try {
            return (T) objectMapper.readValue(objectMapper.writeValueAsString(t), t.getClass());
        } catch (Exception e) {
            logger.warn("copy object failed", e);
            throw new RuntimeException(e);
        }
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
     * 去除字符串末尾指定字符
     *
     * @param remove
     * @param origin
     * @return
     */
    public static String removeEnd(String remove, String origin) {
        if (StringUtils.isAnyBlank(remove, origin)) {
            return origin;
        }
        if (!StringUtils.endsWith(origin, remove)) {
            return origin;
        }
        return removeEnd(remove, origin.substring(0, origin.length() - remove.length()));
    }

}
