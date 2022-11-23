package org.hango.cloud.configuration.mode;

import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import org.hango.cloud.configuration.ApiPlaneAutoBaseConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 网关模式下的configuration
 **/
@Profile({"gw-qz", "gw-yx"})
@Configuration
@AutoConfigureBefore(ApiPlaneAutoBaseConfiguration.class)
public class GatewayAutoConfiguration {

    // 不自动启用grpc server
    @Bean
    public GrpcServerLifecycle grpcServerLifecycle() {
        return null;
    }
}
