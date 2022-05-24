package org.hango.cloud.configuration;

import org.hango.cloud.configuration.env.NonK8sConfiguration;
import org.hango.cloud.core.k8s.GatewayK8sEventWatcher;
import org.hango.cloud.core.k8s.MeshK8sEventWatcher;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.core.slime.SlimeHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *  用于k8s模式的事件处理
 *
 **/
@Configuration
@ConditionalOnMissingBean(NonK8sConfiguration.class)
public class EventConfiguration {

    @Bean
    @Profile("sm")
    MeshK8sEventWatcher meshK8sEventWatcher(MultiClusterK8sClient multiClusterK8sClient) {
        return new MeshK8sEventWatcher(multiClusterK8sClient);
    }

    @Bean
    @Profile("gw-qz")
    GatewayK8sEventWatcher gatewayK8sEventWatcher(SlimeHttpClient slimeHttpClient) {
        return new GatewayK8sEventWatcher(slimeHttpClient);
    }

}
