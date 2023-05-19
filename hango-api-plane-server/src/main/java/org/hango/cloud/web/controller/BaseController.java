package org.hango.cloud.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.hango.cloud.util.errorcode.ApiPlaneErrorCode;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.hango.cloud.util.errorcode.ErrorCodeEnum;
import org.hango.cloud.web.holder.LogTraceUUIDHolder;
import org.hango.cloud.web.holder.RequestContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private ObjectMapper objectMapper;

    String RESULT_LIST = "List";
    String RESULT = "Result";
    protected int SUCCESS = 200;

    public String apiReturn(int statusCode) {
        return apiReturn(statusCode, null, null, null);
    }

    public String apiReturn(int statusCode, String code, String message, Map<String, Object> params) {
        return apiReturn(this.objectMapper, statusCode, code, message, params);
    }

    public String apiReturn(int statusCode, String code, String message, Map<String, Object> params, boolean silent) {
        return apiReturn(this.objectMapper, statusCode, code, message, params, true);
    }

    public String apiReturn(ObjectMapper objectMapper, int statusCode, String code, String message, Map<String, Object> params) {
        return apiReturn(objectMapper, statusCode, code, message, params, false);
    }

    public String apiReturn(ObjectMapper objectMapper, int statusCode, String code, String message, Map<String, Object> params, boolean silent) {
        Map<String, Object> body = new HashMap<>();
        body.put("RequestId", LogTraceUUIDHolder.getUUIDId());

        if (StringUtils.isNotBlank(code)) {
            body.put("Code", code);
        }
        if (StringUtils.isNotBlank(message)) {
            body.put("Message", message);
        }
        if (params != null && !params.isEmpty()) {
            body.putAll(params);
        }
        if (!silent) {
            logger.info("----- Request Id: {}, Response body: {} -----", LogTraceUUIDHolder.getUUIDId(), body);
        }
        HttpServletResponse response = RequestContextHolder.getResponse();
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        response.setStatus(statusCode);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (IOException e) {
            logger.warn("io exception.", e);
        }

        return null;
    }

    public String apiReturn(ErrorCode code) {
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), null);
    }

    public String apiReturn(ErrorCodeEnum code, String errormsg) {
        return apiReturn(code.getStatusCode(), code.getCode(), errormsg, null);
    }

    public String apiReturn(Map<String, Object> params) {
        return apiReturn(ApiPlaneErrorCode.Success.getStatusCode(), ApiPlaneErrorCode.Success.getCode(), null, params);
    }

    public String apiReturnInSilent(Map<String, Object> params) {
        return apiReturn(this.objectMapper, ApiPlaneErrorCode.Success.getStatusCode(), ApiPlaneErrorCode.Success.getCode(), null, params, true);
    }

    public String apiReturnInSilent(ErrorCode code) {
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), null, true);
    }
}
