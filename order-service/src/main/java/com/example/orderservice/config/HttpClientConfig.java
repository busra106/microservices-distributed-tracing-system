package com.example.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {
    @Bean
    public RestTemplate restTemplate() {
        // Spring Cloud Sleuth will automatically decorate RestTemplate
        // so trace headers are propagated to downstream services.
        return new RestTemplate();
    }
}

