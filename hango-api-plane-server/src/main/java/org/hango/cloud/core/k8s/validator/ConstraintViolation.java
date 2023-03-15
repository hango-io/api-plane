package org.hango.cloud.core.k8s.validator;

public class ConstraintViolation<T> {
    private String message;
    private T bean;
    private Object validator;


    public ConstraintViolation(String message, T bean, Object validator) {
        this.message = message;
        this.bean = bean;
        this.validator = validator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public Object getValidator() {
        return validator;
    }

    public void setValidator(Object validator) {
        this.validator = validator;
    }
}
