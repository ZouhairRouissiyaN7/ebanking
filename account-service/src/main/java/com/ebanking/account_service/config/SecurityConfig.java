package com.ebanking.account_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        JwtDecoder jwtDecoder(
                        @Value("${app.security.jwt.jwk-set-uri}") String jwks,
                        @Value("${app.security.jwt.issuer}") String issuer,
                        @Value("${app.security.jwt.audience}") String audience) {
                NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwks)
                                .jwsAlgorithm(
                                                org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                                .build();

                OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                                ? OAuth2TokenValidatorResult.success()
                                : OAuth2TokenValidatorResult.failure(
                                                new OAuth2Error(
                                                                "invalid_token",
                                                                "Invalid JWT audience",
                                                                null));

                decoder.setJwtValidator(
                                new DelegatingOAuth2TokenValidator<>(
                                                JwtValidators.createDefaultWithIssuer(issuer),
                                                audienceValidator));

                return decoder;
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();

                roles.setAuthoritiesClaimName("roles");
                roles.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                converter.setJwtGrantedAuthoritiesConverter(roles);

                return converter;
        }

        @Bean
        SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter converter) throws Exception {

                return http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/actuator/health")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                // Account APIs currently require ADMIN.
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/accounts/current",
                                                                "/api/accounts/saving")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/accounts/**")
                                                .hasAnyRole("ADMIN", "CUSTOMER")

                                                .anyRequest().denyAll())

                                .oauth2ResourceServer(
                                                oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                                .build();
        }
}