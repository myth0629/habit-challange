package com.habitchallenge.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 허용할 오리진 설정
        for (String origin : allowedOrigins) {
            config.addAllowedOrigin(origin);
        }
        
        // 허용할 HTTP 메서드 설정
        config.addAllowedMethod("*");
        
        // 허용할 헤더 설정
        config.addAllowedHeader("*");
        
        // 인증 정보 허용
        config.setAllowCredentials(true);
        
        // 사전 검증(pre-flight) 요청의 캐시 시간(초)
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
