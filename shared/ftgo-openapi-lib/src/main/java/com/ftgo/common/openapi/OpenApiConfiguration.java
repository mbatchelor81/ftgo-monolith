package com.ftgo.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Shared OpenAPI 3.0 configuration that provides a default OpenAPI spec
 * with application metadata. Services can override by defining their own
 * OpenAPI bean.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI ftgoOpenApi(
            @Value("${spring.application.name:FTGO Service}") String applicationName,
            @Value("${ftgo.openapi.version:1.0.0}") String apiVersion,
            @Value("${ftgo.openapi.description:FTGO Microservice API}") String description) {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version(apiVersion)
                        .description(description)
                        .contact(new Contact()
                                .name("FTGO Team")
                                .email("team@ftgo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("Current server")));
    }
}
