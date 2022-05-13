package org.hango.cloud.core.k8s.event;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.context.ApplicationEvent;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/19
 **/
public class K8sResourceDeleteNotificationEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    HasMetadata hmd;

    public K8sResourceDeleteNotificationEvent(Object source) {
        super(source);
    }

    public K8sResourceDeleteNotificationEvent(HasMetadata hasMetadata) {
        super(hasMetadata);
        this.hmd = hasMetadata;
    }

    public HasMetadata getHmd() {
        return hmd;
    }
}
