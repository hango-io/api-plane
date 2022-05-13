package org.hango.cloud.util.errorcode;

/**
 * @author Chen Jiahan | chenjiahan@corp.netease.com
 */
public class ExceptionHandlerErrorCode extends ErrorCode {

    protected ExceptionHandlerErrorCode(ErrorCodeEnum errorCodeEnum, String[] args) {
		super(errorCodeEnum, args);
	}

	public static ErrorCode InternalServerError = new ErrorCode(ErrorCodeEnum.InternalServerError);

	public static ErrorCode UnknownException = new ErrorCode(ErrorCodeEnum.UnknownException);

	public static ErrorCode InvalidBodyFormat = new ErrorCode(ErrorCodeEnum.InvalidBodyFormat);

	public static ErrorCode ResourceConflict = new ErrorCode(ErrorCodeEnum.ResourceConflict);

	public static ErrorCode InvalidParamsError(String paramName,String paramValue){
		return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, paramName, paramValue);
	}

	public static ErrorCode InvalidParameterValue(Object value, String name) {
		return new ErrorCode(ErrorCodeEnum.InvalidParameterValue, name, String.valueOf(value));
	}

	public static ErrorCode BadRequest(String param) {
		return new ErrorCode(ErrorCodeEnum.CustomBadRequest,param);
	}

	public static ErrorCode MissingParamsType(String param) {
		return new ErrorCode(ErrorCodeEnum.MissingParameter, param);
	}

	public static ErrorCode CustomInvalidBodyFormat(String msg) {
		return new ErrorCode(ErrorCodeEnum.InvalidBodyFormatCustom, msg);
	}

}
