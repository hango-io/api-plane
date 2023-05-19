package org.hango.cloud.web.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RequestContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(RequestContextHolder.class);
    public static final String REQUEST_KEY = "__request";
    public static final String RESPONSE_KEY = "__response";
    public static final ThreadLocal<Map<String, Object>> values = new ThreadLocal<>();
    public static final String GLANCE_HEADER = "glance_header";

    public static Object getValue(String key) {
        Map<String, Object> map = values.get();
        if (map == null) {
            logger.warn("未在RequestContextFilter周期中调用getValue方法");
            return null;
//			throw new IllegalStateException("请在RequestContextFilter周期中调用getValue方法!");
        }
        return map.get(key);
    }

    public static void setValue(String key, Object value) {
        Map<String, Object> map = values.get();
        if (map == null) {
            throw new IllegalStateException("请在RequestContextFilter周期中调用setValue方法!");
        } else {
            map.put(key, value);
        }
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) getValue(REQUEST_KEY);
    }

    public static HttpServletResponse getResponse() {
        return (HttpServletResponse) getValue(RESPONSE_KEY);
    }

}