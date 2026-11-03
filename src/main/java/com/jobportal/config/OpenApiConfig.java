package com.jobportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jobPortalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Portal REST API")
                        .description("Enterprise-grade REST API backend for Job Portal. Supports role-based operations for Job Posters and Job Seekers.")
                        .version("v1.0")
                        .contact(new Contact().name("Job Portal Engineering").email("api-support@jobportal.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .schemaRequirement("basicAuth", new SecurityScheme()
                        .name("basicAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("HTTP Basic authentication with registered email and password"));
    }
}
