package com.ftgo.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoOpenApiAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoOpenApiAutoConfiguration.class));

    @Test
    void autoConfigurationRegistersOpenApiBeanWithDefaults() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenAPI.class);
            OpenAPI openApi = context.getBean(OpenAPI.class);
            assertThat(openApi.getInfo().getTitle()).isEqualTo("FTGO Service API");
            assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
        });
    }

    @Test
    void autoConfigurationRespectsCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "ftgo.openapi.title=My Custom API",
                        "ftgo.openapi.version=v2",
                        "ftgo.openapi.description=Custom description")
                .run(context -> {
                    OpenAPI openApi = context.getBean(OpenAPI.class);
                    assertThat(openApi.getInfo().getTitle()).isEqualTo("My Custom API");
                    assertThat(openApi.getInfo().getVersion()).isEqualTo("v2");
                    assertThat(openApi.getInfo().getDescription()).isEqualTo("Custom description");
                });
    }

    @Test
    void autoConfigurationBacksOffWhenCustomBeanExists() {
        contextRunner
                .withBean(OpenAPI.class, () -> new OpenAPI()
                        .info(new io.swagger.v3.oas.models.info.Info()
                                .title("Custom Bean")
                                .version("v99")))
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAPI.class);
                    OpenAPI openApi = context.getBean(OpenAPI.class);
                    assertThat(openApi.getInfo().getTitle()).isEqualTo("Custom Bean");
                });
    }
}
