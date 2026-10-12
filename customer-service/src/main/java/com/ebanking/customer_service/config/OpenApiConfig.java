
package com.ebanking.customer_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8888")
                                .description("Local API Gateway")))
                .info(new Info()
                        .title("eBanking - Customer Service API")
                        .version("1.0.0")
                        .description("REST API for managing banking customers.")
                        .contact(new Contact()
                                .name("eBanking Project")));
    }
}
