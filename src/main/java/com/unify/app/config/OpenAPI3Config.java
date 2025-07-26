package com.unify.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPI3Config {

  private static final String SECURITY_SCHEME_NAME = "Bearer";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(apiInfo())
        .addSecurityItem(securityRequirement())
        .components(securityComponents());
  }

  private Info apiInfo() {
    return new Info()
        .title("Unify Platform APIs")
        .version("1.0.0")
        .description("This documentation provides all REST API definitions for the Unify platform.")
        .contact(
            new Contact()
                .name("Unify Dev Team")
                .email("support.unify@gmail.com")
                .url("https://unifyapp.com"));
  }

  private SecurityRequirement securityRequirement() {
    return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
  }

  private Components securityComponents() {
    return new Components()
        .addSecuritySchemes(
            SECURITY_SCHEME_NAME,
            new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT Bearer token. Example: `Bearer eyJhbGciOi...`"));
  }
}
