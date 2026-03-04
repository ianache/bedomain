package com.bedomain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BeDomain API")
                        .version("1.0.0")
                        .description("Business Entity Domain Management Service API")
                        .contact(new Contact()
                                .name("BeDomain Team")
                                .email("dev@bedomain.com")));
    }
}
