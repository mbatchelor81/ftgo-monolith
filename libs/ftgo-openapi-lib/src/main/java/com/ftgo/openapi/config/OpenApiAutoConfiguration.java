package com.ftgo.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Auto-configuration for OpenAPI 3.0 documentation via SpringDoc.
 * Services can override any bean by declaring their own.
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiAutoConfiguration {

    private final OpenApiProperties properties;

    public OpenApiAutoConfiguration(OpenApiProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI ftgoOpenApi() {
        String email = properties.getContactEmail();
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .contact(new Contact()
                                .name(properties.getContactName())
                                .email(email == null || email.isEmpty() ? null : email))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("Default")));
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .pathsToExclude("/actuator/**")
                .build();
    }
}
