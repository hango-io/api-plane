package org.hango.cloud.util.exception;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/6
 **/
public class ResourceConflictException extends ApiPlaneException {
    public ResourceConflictException() {
    }

    public ResourceConflictException(String message) {
        super(message);
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
