package org.hango.cloud;

import org.hango.cloud.configuration.ext.YamlPropertyLoaderFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;



@SpringBootApplication
@PropertySource(value = {"classpath:k8s.yaml", "classpath:jdbc.yaml","classpath:resourceExtractorConfig.yaml"}, factory = YamlPropertyLoaderFactory.class)
@EnableConfigurationProperties
@EnableScheduling
@EnableTransactionManagement
// 使用cglib动态代理
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
public class ApiPlaneApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ApiPlaneApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApiPlaneApplication.class);
    }
}