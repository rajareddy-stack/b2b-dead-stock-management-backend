package com.deadstock.marketplace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deadStockOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dead Stock Marketplace API")
                        .description("RESTful APIs for the B2B Dead Stock & Surplus Materials Marketplace")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("B2B Marketplace Support")
                                .email("support@deadstockmarketplace.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
