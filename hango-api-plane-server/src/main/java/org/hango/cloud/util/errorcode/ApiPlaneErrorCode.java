package org.hango.cloud.util.errorcode;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/22
 **/
public class ApiPlaneErrorCode {

    public static ErrorCode Success = new ErrorCode(ErrorCodeEnum.Success);

    public static ErrorCode InvalidBodyFormat = new ErrorCode(ErrorCodeEnum.InvalidBodyFormat);

    public static ErrorCode resourceNotFound = resourceNotFoundErrorCode();
    public static ErrorCode workLoadNotFound = workLoadNotFoundErrorCode();
    public static ErrorCode sidecarInjectPolicyError = sidecarInjectPolicyError();
    public static ErrorCode workLoadNotInMesh = workLoadNotInMesh();

    public static ErrorCode InvalidFormat(String param) {
        return new ErrorCode(ErrorCodeEnum.InvalidFormat, param);
    }

    public static ErrorCode ParameterError(String param) {
        return new ErrorCode(ErrorCodeEnum.ParameterError, param);
    }

    public static ErrorCode CanNotFound(String param) {
        return new ErrorCode(ErrorCodeEnum.CanNotFound, param);
    }

    public static ErrorCode MissingParamsError(String paramName) {
        return new ErrorCode(ErrorCodeEnum.MissingParameter, paramName);
    }

    public static ErrorCode sidecarInjectPolicyError(){
        ErrorCode errorCode = new ErrorCode();
        errorCode.setStatusCode(400);
        errorCode.setCode("PolicyError");
        errorCode.setMessage("当前资源所属名称空间已禁用自动注入");
        errorCode.setEnMessage("Injection for pod in namespace is disabled");
        return errorCode;
    }

    private static ErrorCode resourceNotFoundErrorCode() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.ResourceNotFound);
        errorCode.setCode("404");
        errorCode.setMessage("目标配置不存在");
        errorCode.setEnMessage("The target config does not exist");
        return errorCode;
    }

    private static ErrorCode workLoadNotFoundErrorCode() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.ResourceNotFound);
        errorCode.setCode("404");
        errorCode.setMessage("工作负载不存在");
        errorCode.setEnMessage("The workload does not exist");
        return errorCode;
    }

    private static ErrorCode workLoadNotInMesh() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.QueryParameterError, null);
        errorCode.setCode("400");
        errorCode.setMessage("该负载未加入网格");
        errorCode.setEnMessage("The workload is not added to the mesh");
        return errorCode;
    }

    /**
     * 负载均衡相关
     */
    public static ErrorCode InvalidLoadBanlanceType = new ErrorCode(ErrorCodeEnum.InvalidLoadBanlanceType);
    public static ErrorCode InvalidSimpleLoadBanlanceType = new ErrorCode(ErrorCodeEnum.InvalidSimpleLoadBanlanceType);
    public static ErrorCode InvalidConsistentHashObject = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashObject);
    public static ErrorCode InvalidConsistentHashType = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashType);
    public static ErrorCode InvalidConsistentHashHttpCookieObject = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashHttpCookieObject);
    public static ErrorCode InvalidConsistentHashHttpCookieName = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashHttpCookieName);
    public static ErrorCode InvalidConsistentHashHttpCookieTtl = new ErrorCode(ErrorCodeEnum.InvalidConsistentHashHttpCookieTtl);
    public static ErrorCode InvalidHttp1MaxPendingRequests = new ErrorCode(ErrorCodeEnum.InvalidHttp1MaxPendingRequests);
    public static ErrorCode InvalidHttp2MaxRequests = new ErrorCode(ErrorCodeEnum.InvalidHttp2MaxRequests);
    public static ErrorCode InvalidIdleTimeout = new ErrorCode(ErrorCodeEnum.InvalidIdleTimeout);
    public static ErrorCode InvalidMaxRequestsPerConnection = new ErrorCode(ErrorCodeEnum.InvalidMaxRequestsPerConnection);
    public static ErrorCode InvalidMaxConnections = new ErrorCode(ErrorCodeEnum.InvalidMaxConnections);
    public static ErrorCode InvalidConnectTimeout = new ErrorCode(ErrorCodeEnum.InvalidConnectTimeout);

}
