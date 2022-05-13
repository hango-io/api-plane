package org.hango.cloud.web.filter;


import org.hango.cloud.web.holder.RequestContextHolder;

import javax.servlet.*;
import java.io.IOException;
import java.util.HashMap;

public class RequestContextHolderFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        RequestContextHolder.values.set(new HashMap<String, Object>());
        RequestContextHolder.setValue(RequestContextHolder.REQUEST_KEY, servletRequest);
        RequestContextHolder.setValue(RequestContextHolder.RESPONSE_KEY, servletResponse);
        filterChain.doFilter(servletRequest, servletResponse);
        RequestContextHolder.values.set(null);
    }

    @Override
    public void destroy() {

    }
}
