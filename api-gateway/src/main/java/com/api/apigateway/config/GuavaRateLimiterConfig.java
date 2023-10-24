package com.api.apigateway.config;


import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Configuration
public class GuavaRateLimiterConfig {


    @SuppressWarnings("UnstableApiUsage")
    @Bean
    public RateLimiter rateLimiter(){
        /*每秒控制5个许可*/
        return RateLimiter.create(20);
    }
}
