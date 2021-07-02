package org.hango.cloud.util.exception;

public class ApiPlaneException extends RuntimeException {

    private Integer status;

    public ApiPlaneException() {}

    public ApiPlaneException(String message) {
        super(message);
    }

    public ApiPlaneException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiPlaneException(String message, int status) {
        super(message);
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
