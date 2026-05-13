package com.ftgo.security.jwt;

import com.ftgo.security.config.FtgoSecurityAutoConfiguration;
import com.ftgo.security.exception.SecurityExceptionHandlers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoJwtAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    WebMvcAutoConfiguration.class,
                    FtgoSecurityAutoConfiguration.class,
                    FtgoJwtAutoConfiguration.class));

    @Test
    void jwtBeansAreNotRegisteredWhenDisabled() {
        contextRunner
                .withPropertyValues("ftgo.security.jwt.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JwtClaimsExtractor.class);
                    assertThat(context).doesNotHaveBean(FtgoJwtAuthenticationConverter.class);
                    assertThat(context).doesNotHaveBean(TokenRefreshService.class);
                });
    }

    @Test
    void jwtBeansAreNotRegisteredByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(JwtClaimsExtractor.class);
            assertThat(context).doesNotHaveBean(FtgoJwtAuthenticationConverter.class);
        });
    }

    @Test
    void jwtBeansAreRegisteredWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "ftgo.security.jwt.enabled=true",
                        "ftgo.security.jwt.jwk-set-uri=http://localhost:8180/realms/ftgo/protocol/openid-connect/certs",
                        "ftgo.security.jwt.issuer-uri=http://localhost:8180/realms/ftgo")
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtClaimsExtractor.class);
                    assertThat(context).hasSingleBean(FtgoJwtAuthenticationConverter.class);
                    assertThat(context).hasSingleBean(TokenRefreshService.class);
                    assertThat(context).hasSingleBean(SecurityExceptionHandlers.class);
                    assertThat(context).hasSingleBean(SecurityFilterChain.class);
                    assertThat(context).hasSingleBean(JwtDecoder.class);
                });
    }

    @Test
    void jwtPropertiesAreConfigurable() {
        contextRunner
                .withPropertyValues(
                        "ftgo.security.jwt.enabled=true",
                        "ftgo.security.jwt.jwk-set-uri=http://keycloak:8080/certs",
                        "ftgo.security.jwt.issuer-uri=http://keycloak:8080/realm",
                        "ftgo.security.jwt.roles-claim-name=custom_roles",
                        "ftgo.security.jwt.permissions-claim-name=custom_perms",
                        "ftgo.security.jwt.user-id-claim-name=user_id",
                        "ftgo.security.jwt.role-prefix=PERM_")
                .run(context -> {
                    FtgoJwtProperties props = context.getBean(FtgoJwtProperties.class);
                    assertThat(props.getRolesClaimName()).isEqualTo("custom_roles");
                    assertThat(props.getPermissionsClaimName()).isEqualTo("custom_perms");
                    assertThat(props.getUserIdClaimName()).isEqualTo("user_id");
                    assertThat(props.getRolePrefix()).isEqualTo("PERM_");
                });
    }

    @Test
    void tokenRefreshPropertiesAreConfigurable() {
        contextRunner
                .withPropertyValues(
                        "ftgo.security.jwt.enabled=true",
                        "ftgo.security.jwt.jwk-set-uri=http://keycloak:8080/certs",
                        "ftgo.security.jwt.token-refresh.enabled=true",
                        "ftgo.security.jwt.token-refresh.refresh-before-expiry-seconds=600",
                        "ftgo.security.jwt.token-refresh.token-endpoint=http://keycloak:8080/token",
                        "ftgo.security.jwt.token-refresh.client-id=ftgo-api")
                .run(context -> {
                    FtgoJwtProperties props = context.getBean(FtgoJwtProperties.class);
                    assertThat(props.getTokenRefresh().isEnabled()).isTrue();
                    assertThat(props.getTokenRefresh().getRefreshBeforeExpirySeconds()).isEqualTo(600);
                    assertThat(props.getTokenRefresh().getTokenEndpoint()).isEqualTo("http://keycloak:8080/token");
                    assertThat(props.getTokenRefresh().getClientId()).isEqualTo("ftgo-api");
                });
    }
}
