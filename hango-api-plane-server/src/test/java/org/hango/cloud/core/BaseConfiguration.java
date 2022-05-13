package org.hango.cloud.core;

import net.devh.springboot.autoconfigure.grpc.server.GrpcServerLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/18
 **/
@Configuration
public class BaseConfiguration {

    @Bean
    public GrpcServerLifecycle grpcServerLifecycle() {
        return null;
    }

}
