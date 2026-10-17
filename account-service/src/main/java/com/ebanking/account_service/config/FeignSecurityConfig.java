package com.ebanking.account_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignSecurityConfig {

    @Bean
    public RequestInterceptor bearerTokenInterceptor() {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext()
                    .getAuthentication();

            if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
                    || !authentication.isAuthenticated()) {
                throw new IllegalStateException(
                        "No authenticated JWT available for customer lookup");
            }

            String token = jwtAuth.getToken().getTokenValue();

            template.header(
                    "Authorization",
                    "Bearer " + token);
        };
    }
}