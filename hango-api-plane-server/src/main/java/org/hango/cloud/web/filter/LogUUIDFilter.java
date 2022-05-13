package org.hango.cloud.web.filter;

import org.hango.cloud.web.holder.LogTraceUUIDHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.UUID;

public class LogUUIDFilter implements Filter {
	
	private static final Logger logger = LoggerFactory.getLogger(LogUUIDFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			String uniqueId = UUID.randomUUID().toString();
			
			StringBuilder uuidBuilder = new StringBuilder(LogTraceUUIDHolder.LOG_TRACE_PREFIX);
			uuidBuilder.append(uniqueId);
			
			MDC.put(LogTraceUUIDHolder.LOG_TRACE_KEY, uuidBuilder.toString());
			LogTraceUUIDHolder.setUUIDId(uniqueId);

			chain.doFilter(request, response);
			
		} catch(Exception e) {
			logger.info("", e);
			
		}
	}

	@Override
	public void destroy() {
		MDC.remove(LogTraceUUIDHolder.LOG_TRACE_KEY);
	}

}
