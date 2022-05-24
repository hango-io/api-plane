package org.hango.cloud.core.k8s.event;

import org.springframework.context.ApplicationEvent;

/**
 * 该事件用于rate limit server 的configmap更新后，提醒修改rate limit server的annotation
 *
 **/
public class NotifyRateLimitServerEvent extends ApplicationEvent {

    public NotifyRateLimitServerEvent(RlsInfo rlsInfo) {
        super(rlsInfo);
    }

}
