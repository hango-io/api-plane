package org.hango.cloud.util.advice;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import org.hango.cloud.util.errorcode.ErrorCode;
import org.hango.cloud.util.errorcode.ExceptionHandlerErrorCode;
import org.hango.cloud.util.exception.ApiPlaneException;
import org.hango.cloud.util.exception.ResourceConflictException;
import org.hango.cloud.web.holder.LogTraceUUIDHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;


/**
 * @author Chen Jiahan | chenjiahan@corp.netease.com
 */
@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommonExceptionHandler.class);

    private static final String EXCEPTION_PREFIX = "Exception:";

    /**
     * ------ 子类新增捕获 -------
     **/

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception exception) {
        return newResponse(exception.getMessage(), "Internal Exception", 500, exception);
    }


    @ExceptionHandler(value = JsonParseException.class)
    public ResponseEntity<Object> JSONExceptionExceptionHandler(JsonParseException ex) {
        return newResponse(ExceptionHandlerErrorCode.InvalidBodyFormat, ex);
    }


    @ExceptionHandler(JsonPathException.class)
    public ResponseEntity<Object> handleJsonPathException(Exception exception) {

        String msg = exception.getMessage();
        int begin = 0;
        int end = msg.length();
        if (msg.contains(EXCEPTION_PREFIX)) begin = msg.indexOf(EXCEPTION_PREFIX) + EXCEPTION_PREFIX.length();
        if (msg.contains("(")) end = msg.indexOf("(");

        return newResponse(msg.substring(begin, end).trim(), "JSON Exception", 400, exception);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return newResponse("illegal argument " + ex.getMessage(), "Illegal Argument", 400, ex);
    }

    /*
     *	参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgsTypeMismatchException(MethodArgumentTypeMismatchException exception) {

        ErrorCode errorCode = ExceptionHandlerErrorCode.InvalidParamsError(exception.getName(), exception.getValue().toString());
        return newResponse(errorCode, exception);
    }

    @ExceptionHandler(InvalidJsonException.class)
    public ResponseEntity<Object> handleInvalidJsonException(InvalidJsonException exception) {
        ErrorCode errorCode = ExceptionHandlerErrorCode.InvalidBodyFormat;
        return newResponse(errorCode, exception);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Object> handleResourceConflictException(ResourceConflictException exception) {
        ErrorCode errorCode = ExceptionHandlerErrorCode.ResourceConflict;
        return newResponse(errorCode, exception);
    }

    @ExceptionHandler(ApiPlaneException.class)
    public ResponseEntity<Object> handleServiceMeshApiException(ApiPlaneException exception) {
        ErrorCode errorCode = ExceptionHandlerErrorCode.BadRequest(exception.getMessage());
        return newResponse(errorCode, exception);
    }

    /**
     * ------ 覆盖父类处理方法 -------
     **/


    /*
     * 参数值不合理
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {


        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null) {
            ErrorCode errorCode = ExceptionHandlerErrorCode.InvalidParamsError(
                    fieldError.getDefaultMessage(), String.valueOf(fieldError.getRejectedValue()));
            return newResponse(errorCode, exception);
        }

        List<ObjectError> errors = exception.getBindingResult().getAllErrors();
        if (CollectionUtils.isEmpty(errors)) {
            return handleException(exception);
        }

        String message = errors.stream().findFirst().get().getDefaultMessage();
        ErrorCode errorCode = ExceptionHandlerErrorCode.BadRequest(message);
        return newResponse(errorCode, exception);
    }

    /*
     * 参数缺失
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        return newResponse(ExceptionHandlerErrorCode.MissingParamsType(ex.getParameterName()), ex);
    }

    /*
     * API不存在
     */
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        return newResponse(ExceptionHandlerErrorCode.BadRequest(ex.getMessage()), ex);
    }

    /*
     * Body不存在
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (ex.getMessage().startsWith("Required request body is missing")) {
            return newResponse(ExceptionHandlerErrorCode.InvalidBodyFormat, ex);
        }
        if (ex.getCause().getClass().isAssignableFrom(JsonMappingException.class)) {
            return newResponse(ExceptionHandlerErrorCode.CustomInvalidBodyFormat(ex.getMessage()), ex);
        }
        if (ex.getCause().getClass().isAssignableFrom(InvalidFormatException.class)) {
            return newResponse(ExceptionHandlerErrorCode.InvalidBodyFormat, ex);
        }
        return newResponse(ExceptionHandlerErrorCode.InternalServerError, ex);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        final ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
        final String[] splits = violation.getPropertyPath().toString().split("\\.");
        return newResponse(ExceptionHandlerErrorCode.InvalidParameterValue(violation.getInvalidValue(), splits[splits.length - 1]), ex);
    }

    /*
     * 部分Spring抛出异常未处理，在这里做处理
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String requestId = LogTraceUUIDHolder.getUUIDId();
        if (body == null) {
            CommonReturnEntity returnEntity = CommonReturnEntity.builder()
                    .requestId(requestId)
                    .message(ex.getMessage())
                    .code(ex.getClass().getSimpleName())
                    .build();
            body = returnEntity;
        }
        logger.warn("Common exception handler catch :", ex);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }


    public ResponseEntity<Object> newResponse(ErrorCode errorCode, Exception exception) {
        return newResponse(errorCode.getMessage(), errorCode.getCode(), errorCode.getStatusCode(), exception);
    }

    public ResponseEntity<Object> newResponse(ErrorCode errorCode, ApiPlaneException exception) {
        int status = exception.getStatus() != null ? exception.getStatus() : errorCode.getStatusCode();
        return newResponse(errorCode.getMessage(), errorCode.getCode(), status, exception);
    }


    public ResponseEntity<Object> newResponse(String msg, String code, int statusCode, Exception exception) {

        CommonReturnEntity returnEntity = CommonReturnEntity.builder()
                .requestId(LogTraceUUIDHolder.getUUIDId())
                .message(msg)
                .code(code)
                .build();
        logger.error("Request Id: {}, Common exception handler catch :", LogTraceUUIDHolder.getUUIDId(), exception);

        return new ResponseEntity<>(returnEntity, HttpStatus.valueOf(statusCode));
    }
}

