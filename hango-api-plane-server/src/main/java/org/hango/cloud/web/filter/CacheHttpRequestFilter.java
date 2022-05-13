package org.hango.cloud.web.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
public class CacheHttpRequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;

        if (!isMultiPart(servletRequest)) {
            chain.doFilter(new CacheHttpServletRequestWrapper(servletRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    private boolean isMultiPart(HttpServletRequest request) {
        if (!"post".equals(request.getMethod().toLowerCase())) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase().startsWith("multipart/")) {
            return true;
        }
        return false;
    }
}
