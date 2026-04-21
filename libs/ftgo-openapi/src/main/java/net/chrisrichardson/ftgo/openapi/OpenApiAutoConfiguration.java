package net.chrisrichardson.ftgo.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Auto-configures a single {@link OpenAPI} bean for an FTGO microservice
 * based on {@link OpenApiProperties}.
 *
 * <p>Backs off whenever the application already declares its own
 * {@code OpenAPI} bean, so services that need a fully custom spec can opt
 * out cleanly.
 *
 * <p>Published via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * so services pick up the configuration just by adding a dependency on the
 * {@code ftgo-openapi} library.
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiAutoConfiguration {

  /** Security scheme name referenced by {@code @SecurityRequirement(name = ...)}. */
  public static final String BEARER_JWT_SCHEME = "bearer-jwt";

  @Bean
  @ConditionalOnMissingBean
  public OpenAPI ftgoOpenAPI(OpenApiProperties properties) {
    Info info = new Info()
        .title(properties.getTitle())
        .description(properties.getDescription())
        .version(properties.getVersion())
        .termsOfService(properties.getTermsOfService())
        .contact(new Contact()
            .name(properties.getContact().getName())
            .email(properties.getContact().getEmail())
            .url(properties.getContact().getUrl()))
        .license(new License()
            .name(properties.getLicense().getName())
            .url(properties.getLicense().getUrl()));

    ExternalDocumentation externalDocs = new ExternalDocumentation()
        .description("FTGO REST API standards")
        .url("https://github.com/mbatchelor81/ftgo-monolith/blob/master/docs/rest-api-standards.md");

    SecurityScheme bearerJwt = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("Bearer JWT issued by the FTGO identity service (see EM-40).");

    return new OpenAPI()
        .info(info)
        .externalDocs(externalDocs)
        .servers(List.of(new Server().url("/").description("Current host")))
        .components(new Components().addSecuritySchemes(BEARER_JWT_SCHEME, bearerJwt));
  }
}
