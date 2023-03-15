package org.hango.cloud.meta.dto;

import org.hango.cloud.util.errorcode.ErrorCodeEnum;
import org.springframework.util.StringUtils;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:01
 **/
public class ApiPlaneResult<T> {
    /**
     * 错误码
     */
    private ErrorCodeEnum errorCode;

    /**
     * 错误码
     */
    private String errorMsg;
    /**
     * 返回数据
     */
    private T data;
    /**
     * 是否成功
     */
    private boolean success;

    ApiPlaneResult(){
    }

    private ApiPlaneResult(ErrorCodeEnum errorCode, String errorMsg, T data, boolean success){
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
        this.success = success;
    }

    public Boolean isFailed(){
        return Boolean.FALSE.equals(success);
    }

    public Boolean isSuccess(){
        return Boolean.TRUE.equals(success);
    }

    public static <T> ApiPlaneResult<T> ofSuccess(T data){
        return new ApiPlaneResult<>(null, null, data, true);
    }

    public static <T> ApiPlaneResult<T> ofFailed(ErrorCodeEnum errorCode, String errorMsg){
        return new ApiPlaneResult<>(errorCode, errorMsg, null, false);
    }

    public static <T> ApiPlaneResult<T> ofFailed(ErrorCodeEnum errorCode){
        return new ApiPlaneResult<>(errorCode, errorCode.getEnMsg(), null, false);
    }


    public static <T> ApiPlaneResult<T> ofHttpRemoteError(String errorMsg){
        return new ApiPlaneResult<>(ErrorCodeEnum.HttpRemoteError, errorMsg, null, false);
    }

    public static <T> ApiPlaneResult<T> ofInvaildParam(String errorMsg){
        return new ApiPlaneResult<>(ErrorCodeEnum.InvalidParameters, errorMsg, null, false);
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCodeEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        if (StringUtils.isEmpty(errorMsg) && errorCode != null){
            return errorCode.getEnMsg();
        }
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
