package org.hango.cloud.configuration.ext;

import org.hango.cloud.web.filter.CacheHttpRequestFilter;
import org.hango.cloud.web.filter.LogUUIDFilter;
import org.hango.cloud.web.filter.RequestContextHolderFilter;
import org.hango.cloud.web.interceptor.RequestLogInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebServletConfig extends WebMvcConfigurerAdapter {

	@Bean
	public FilterRegistrationBean requestContextFilterReg() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new RequestContextHolderFilter());
		registration.addUrlPatterns("/*");
		registration.setName(RequestContextHolderFilter.class.getSimpleName());
		registration.setOrder(0);
		return registration;
	}

	@Bean
	public FilterRegistrationBean logUuidFilterReg() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new LogUUIDFilter());
		registration.addUrlPatterns("/*");
		registration.setName(LogUUIDFilter.class.getSimpleName());
		registration.setOrder(1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean cacheHttpRequestFilter() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new CacheHttpRequestFilter());
		registration.addUrlPatterns("/*");
		registration.setName(CacheHttpRequestFilter.class.getSimpleName());
		registration.setOrder(1);
		return registration;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RequestLogInterceptor()).addPathPatterns("/**");
		super.addInterceptors(registry);
	}
}
