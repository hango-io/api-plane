package org.hango.cloud.util.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * 为restTemplate增加日志
 * @author Chen Jiahan | chenjiahan@corp.netease.com
 */
public class RestTemplateLogInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(RestTemplateLogInterceptor.class);
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		
		logger.info("Request  --> URI: {}, Request Body: {}", request.getURI(), new String(body, "UTF-8"));
		ClientHttpResponse response = execution.execute(request, body);
		logger.info("Response <-- Status code: {}", response.getStatusCode());
		
		return response;
	}



}
