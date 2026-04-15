package com.ftgo.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Auto-configuration for FTGO OpenAPI 3.0 documentation.
 *
 * <p>Provides a default {@link OpenAPI} bean configured from {@link FtgoOpenApiProperties}.
 * Services can override this bean by declaring their own {@code OpenAPI} {@code @Bean}.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(FtgoOpenApiProperties.class)
public class FtgoOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI ftgoOpenApi(FtgoOpenApiProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .contact(new Contact()
                                .name(properties.getContactName())
                                .email(properties.getContactEmail())
                                .url(properties.getContactUrl()))
                        .license(new License()
                                .name(properties.getLicenseName())
                                .url(properties.getLicenseUrl())))
                .servers(List.of(
                        new Server().url("/").description("Default Server")));
    }
}
