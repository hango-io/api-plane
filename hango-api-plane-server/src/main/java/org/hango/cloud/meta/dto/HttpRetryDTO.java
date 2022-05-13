package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author hanjiahao
 * 路由重试Dto，重试次数，重试超时时间，重试条件
 */
public class HttpRetryDTO {

    @JsonProperty(value = "IsRetry")
    private boolean retry;

    /**
     * 重试次数
     */
    @JsonProperty(value = "Attempts")
    private int attempts;
    /**
     * 重试超时时间，默认为ms
     */
    @JsonProperty(value = "PerTryTimeout")
    private long perTryTimeout;
    /**
     * 重试条件，，分割
     */
    @JsonProperty(value = "RetryOn")
    private String retryOn;


    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public long getPerTryTimeout() {
        return perTryTimeout;
    }

    public void setPerTryTimeout(long perTryTimeout) {
        this.perTryTimeout = perTryTimeout;
    }

    public String getRetryOn() {
        return retryOn;
    }

    public void setRetryOn(String retryOn) {
        this.retryOn = retryOn;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
