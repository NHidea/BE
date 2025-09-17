package com.NH.Fintech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fintechOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NH Fintech API")
                        .description("NH Fintech 서비스 API 명세서 (Swagger UI)")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("NH Fintech Dev Team")
                                .email("support@nhfintech.com")
                                .url("https://nhfintech.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                );
    }
}