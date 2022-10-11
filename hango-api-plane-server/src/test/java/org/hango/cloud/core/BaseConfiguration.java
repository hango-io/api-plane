package org.hango.cloud.core;

import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BaseConfiguration {

    @Bean
    public GrpcServerLifecycle grpcServerLifecycle() {
        return null;
    }

}
