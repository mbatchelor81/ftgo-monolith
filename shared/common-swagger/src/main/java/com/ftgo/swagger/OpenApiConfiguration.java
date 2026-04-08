package com.ftgo.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Shared OpenAPI 3.0 configuration for FTGO microservices.
 *
 * <p>Migrated from Springfox Swagger 2.x ({@code CommonSwaggerConfiguration})
 * to SpringDoc OpenAPI 3.0 for Spring Boot 3.x compatibility.</p>
 *
 * <p>Each service can override the title and description via Spring properties:</p>
 * <pre>
 *   ftgo.api.title=FTGO Order Service
 *   ftgo.api.description=Manages order lifecycle
 * </pre>
 *
 * @see <a href="https://springdoc.org/">SpringDoc OpenAPI</a>
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${ftgo.api.title:FTGO Service}")
    private String apiTitle;

    @Value("${ftgo.api.description:FTGO Microservice API}")
    private String apiDescription;

    @Value("${ftgo.api.version:1.0.0}")
    private String apiVersion;

    @Bean
    public OpenAPI ftgoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(apiTitle)
                        .description(apiDescription)
                        .version(apiVersion)
                        .contact(new Contact()
                                .name("FTGO Engineering")
                                .email("engineering@ftgo.com")
                                .url("https://github.com/mbatchelor81/ftgo-monolith"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Current server")));
    }
}
