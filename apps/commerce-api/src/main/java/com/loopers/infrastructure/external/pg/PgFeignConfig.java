package com.loopers.infrastructure.external.pg;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PgFeignConfig {

    @Bean
    public RequestInterceptor userIdHeaderInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-USER-ID", "135135"); // 예시: 고정값
            }
        };
    }
}
