package com.ebanking.account_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI accountOpenAPI() {
                return new OpenAPI()
                                .components(new Components()
                                                .addSecuritySchemes(
                                                                "bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .addSecurityItem(
                                                new SecurityRequirement().addList("bearerAuth"))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8888")
                                                                .description("Local API Gateway")))
                                .info(new Info()
                                                .title("eBanking - Account Service API")
                                                .version("1.0.0")
                                                .description("""
                                                                Banking account and transaction management API.
                                                                Financial operations require an Idempotency-Key.
                                                                """)
                                                .contact(new Contact()
                                                                .name("eBanking Project")));
        }
}