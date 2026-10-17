package com.ebanking.auth_service.config;

import com.ebanking.auth_service.entities.AppUser;
import com.ebanking.auth_service.enums.Role;
import com.ebanking.auth_service.repositories.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class AdminInitializer {

    @Bean
    CommandLineRunner createAdmin(
            AppUserRepository repository,
            PasswordEncoder encoder,
            @Value("${app.auth.admin-username}") String username,
            @Value("${app.auth.admin-password}") String password) {
        return args -> {
            if (!repository.existsByUsername(username)) {
                repository.save(
                        new AppUser(
                                username,
                                encoder.encode(password),
                                Role.ADMIN,
                                null));

                System.out.println(
                        "Development admin account created");
            }
        };
    }
}