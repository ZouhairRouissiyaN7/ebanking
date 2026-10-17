
package com.ebanking.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import com.nimbusds.jose.jwk.RSAKey;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;

import com.ebanking.auth_service.repositories.AppUserRepository;
import com.ebanking.auth_service.entities.AppUser;

import java.util.List;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(
                        AppUserRepository repository) {
                return username -> {
                        AppUser user = repository.findByUsername(username)
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "User not found"));

                        return User.withUsername(user.getUsername())
                                        .password(user.getPasswordHash())
                                        .roles(user.getRole().name())
                                        .disabled(!user.isEnabled())
                                        .build();
                };
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        UserDetailsService users,
                        PasswordEncoder encoder) {
                var provider = new DaoAuthenticationProvider(users);
                provider.setPasswordEncoder(encoder);

                return new ProviderManager(provider);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationConverter converter) throws Exception {

                return http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/login")
                                                .permitAll()
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/.well-known/jwks.json",
                                                                "/actuator/health")
                                                .permitAll()
                                                .requestMatchers("/api/auth/users/**")
                                                .hasRole("ADMIN")
                                                .anyRequest().denyAll())
                                .oauth2ResourceServer(
                                                oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                                .build();
        }

        @Bean
        public JwtDecoder jwtDecoder(
                        RSAKey rsaKey,
                        @Value("${app.jwt.issuer}") String issuer) {
                NimbusJwtDecoder decoder = NimbusJwtDecoder
                                .withPublicKey(rsaKey.toRSAPublicKey())
                                .build();

                OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains("ebanking-api")
                                ? OAuth2TokenValidatorResult.success()
                                : OAuth2TokenValidatorResult.failure(
                                                new OAuth2Error(
                                                                "invalid_token",
                                                                "Invalid audience",
                                                                null));

                decoder.setJwtValidator(
                                new DelegatingOAuth2TokenValidator<>(
                                                JwtValidators.createDefaultWithIssuer(issuer),
                                                audienceValidator));

                return decoder;
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();

                roles.setAuthoritiesClaimName("roles");
                roles.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                converter.setJwtGrantedAuthoritiesConverter(roles);

                return converter;
        }

}
