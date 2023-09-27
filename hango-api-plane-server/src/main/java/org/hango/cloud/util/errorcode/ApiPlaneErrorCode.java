package org.hango.cloud.util.errorcode;


public class ApiPlaneErrorCode {

    public static ErrorCode Success = new ErrorCode(ErrorCodeEnum.Success);

    public static ErrorCode ParameterError(String param) {
        return new ErrorCode(ErrorCodeEnum.ParameterError, param);
    }

    /**
     * 负载均衡相关
     */
    public static ErrorCode InvalidSlowStartWindow = new ErrorCode(ErrorCodeEnum.InvalidSlowStartWindow);
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
    public static ErrorCode PluginOrderPortError = new ErrorCode(ErrorCodeEnum.PluginOrderPortError);


}
