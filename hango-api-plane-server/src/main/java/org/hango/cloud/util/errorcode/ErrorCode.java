package org.hango.cloud.util.errorcode;


import org.hango.cloud.web.holder.RequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 新版 OpenAPI 后使用的 ErrorCode，该 ErrorCode 是对基础枚举 ErrorCodeEnum 的封装
 * 由于 ErrorCodeEnum 的 message 进行格式化时需要的参数个数不确定，为了防止少传、多传、误传参数，则使用 *ErrorCode 进行再一次封装
 * <p>
 * 不同的服务使用不同的 *ErrorCode，如：用户网关G0使用的是 CommonErrorCode
 *
 */
public class ErrorCode {
    private static Logger logger = LoggerFactory.getLogger(ErrorCode.class);

    public static final String SUCCESS = "Success";

    private String code;
    private String message;
    private String enMessage;
    private int statusCode;

    public ErrorCode() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        HttpServletRequest request = RequestContextHolder.getRequest();
        if (null != request && "zh".equals(request.getHeader("X-163-AcceptLanguage"))) {
            return message;
        } else {
            return enMessage;
        }
    }

    public void setEnMessage(String enMessage) {
        this.enMessage = enMessage;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    protected ErrorCode(ErrorCodeEnum errorCodeEnum, String... args) {
        try {
            this.statusCode = errorCodeEnum.getStatusCode();
            this.code = errorCodeEnum.getCode();
            this.message = String.format(errorCodeEnum.getMsg(), args);
            this.enMessage = String.format(errorCodeEnum.getEnMsg(), args);
        } catch (Exception e) {
            logger.error("ErrorCode 中string.format异常, 请立即检查!", e);
            this.message = errorCodeEnum.getEnMsg();
        }
    }
}