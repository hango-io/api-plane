package org.hango.cloud.web.interceptor;

import org.hango.cloud.web.holder.LogTraceUUIDHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;


public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        if (uri.endsWith("api/health")) return true;
        String method = request.getMethod();
        String queryString = request.getQueryString();
        String body = StreamUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));
        logger.info("----- Request Id: {}, Request Method: {}, Uri: {}?{} -----", LogTraceUUIDHolder
            .getUUIDId(), method, uri, queryString);
        if (!StringUtils.isEmpty(body)) {
            logger.info("Request Body:{}", body);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
