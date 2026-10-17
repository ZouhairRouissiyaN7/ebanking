
package com.ebanking.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

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

import com.ebanking.auth_service.repositories.AppUserRepository;
import com.ebanking.auth_service.entities.AppUser;

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
                        HttpSecurity http) throws Exception {

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
                                                .anyRequest().denyAll())
                                .build();
        }
}
