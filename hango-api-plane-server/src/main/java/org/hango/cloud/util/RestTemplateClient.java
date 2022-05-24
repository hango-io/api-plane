package org.hango.cloud.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description:
 *
 * @create: 2018-12-18
 **/
@Component
public class RestTemplateClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateClient.class);

    private static final String DEFAULT_ERROR_MSG = "无效的请求参数";
    private static final String INNER_ERROR_MSG = "内部组件访问异常";
    private static final String HTTP_PREFIX = "http://";
    ObjectMapper jsonMapper = new ObjectMapper();


    public <T> T getForValueWithHeaders(String url, Object request, String method, Class<T> responseType, HttpHeaders requestHeaders) {
        ResponseEntity<T> responseEntity = null;
        try {
            url = addHttpPrexfix(url);
            HttpEntity requestEntity;
            if (Const.GET_METHOD.equals(method)) {
                Map<String, Object> param = (Map<String, Object>) request;
                requestEntity = new HttpEntity<>(null, requestHeaders);
                responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType, param);
            } else if (Const.POST_METHOD.equals(method)) {
                requestEntity = new HttpEntity<>(request, requestHeaders);
                responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
            }
        } catch (HttpClientErrorException e) {
            String response = e.getResponseBodyAsString();
            logger.info("error response:{}", response);
            throw new ApiPlaneException(parseErrorMsg(response), e.getCause());
        } catch (HttpServerErrorException e) {
            throw new ApiPlaneException(INNER_ERROR_MSG, e.getCause());
        } catch (RestClientException e) {
            throw new ApiPlaneException(e.getMessage(), e.getCause());
        }
        return (responseEntity ==null)?null:responseEntity.getBody();
    }


    public <T> T getForValue(String url, Object request, String method, Class<T> responseType) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Interface-Type", Const.INTERFACE_CALL_TYPE_INNER);
        requestHeaders.add("X-163-AcceptLanguage", Const.ACCEPT_LANGUAGE_ZH);
        requestHeaders.add("Content-Type", "application/json;charset=UTF-8");
        return getForValueWithHeaders(url, request, method, responseType, requestHeaders);
    }


    public <T> T getObjectFromResponse(String key, Class<T> resType, String body) {
        try {
            JsonNode bodyNode = jsonMapper.readTree(body);
            String stringObject = bodyNode.get(key).asText();
            if (StringUtils.isBlank(stringObject)){
                return null;
            }
            return jsonMapper.readValue(stringObject,resType);
        } catch (IOException e) {
            logger.warn("Get object from response error");
            throw new ApiPlaneException(e.getMessage(), e.getCause());
        }
    }

    public <T> List<T> getArrayFromResponse(String key, Class<T> resType, String body) {
        try {
            JsonNode bodyNode = jsonMapper.readTree(body);
            String stringObject = bodyNode.get(key).asText();
            if (StringUtils.isBlank(stringObject)) {
                return new ArrayList<>();
            }
            CollectionType responseType = jsonMapper.getTypeFactory()
                    .constructCollectionType(List.class, resType);
            return jsonMapper.readValue(stringObject, responseType);
        } catch (IOException e) {
            logger.warn("Get array from response error");
            throw new ApiPlaneException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 尝试从响应报文中解析出Message信息
     */
    private String parseErrorMsg(String responseBody) {
        try {
            JsonNode bodyNode = jsonMapper.readTree(responseBody);
            if (!StringUtils.isBlank(bodyNode.get("message").asText())){
                return bodyNode.get("message").asText();
            }
            if (!StringUtils.isBlank(bodyNode.get("Message").asText())){
                return bodyNode.get("Message").asText();
            }
            return DEFAULT_ERROR_MSG;
        } catch (IOException e) {
            logger.warn("Get error message from response error");
            throw new ApiPlaneException(e.getMessage(), e.getCause());
        }
    }

    private String addHttpPrexfix(String url) {
        if (!url.startsWith("HTTP") && !url.startsWith("http")) {
            return HTTP_PREFIX + url;
        }
        return url;
    }

    /**
     * 根据请求参数Map拼接请求URL
     */
    public String buildRequestUrlWithParameter(String url, Map<String, String> param) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if (param == null || param.isEmpty()) {
            return url;
        }
        param.forEach((k, v) -> {
            if (!StringUtils.isEmpty(k)) {
                urlBuilder.append("&")
                        //默认URL中请求参数名首字母大写
                        .append(k.substring(0, 1).toUpperCase() + k.substring(1))
                        .append("=")
                        .append("{" + k + "}");
            }
        });
        return urlBuilder.toString();
    }


}
