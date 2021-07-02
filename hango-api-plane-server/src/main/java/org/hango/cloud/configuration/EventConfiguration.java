package org.hango.cloud.configuration;

import org.hango.cloud.core.slime.SlimeHttpClient;
import org.hango.cloud.core.k8s.GatewayK8sEventWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class EventConfiguration {

    @Bean
//    @Profile("gw-qz")
    GatewayK8sEventWatcher gatewayK8sEventWatcher(SlimeHttpClient slimeHttpClient) {
        return new GatewayK8sEventWatcher(slimeHttpClient);
    }

}
