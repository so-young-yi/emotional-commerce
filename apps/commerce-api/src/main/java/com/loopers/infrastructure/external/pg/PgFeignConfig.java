package com.loopers.infrastructure.external.pg;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PgFeignConfig {

    @Value("${client.pg-simulator.x-user-id}")
    private Long userId;

    @Bean
    public RequestInterceptor userIdHeaderInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-USER-ID", String.valueOf(userId));
            }
        };
    }
}
