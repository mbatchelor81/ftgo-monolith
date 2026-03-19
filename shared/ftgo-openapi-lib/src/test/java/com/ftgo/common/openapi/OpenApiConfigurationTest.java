package com.ftgo.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.application.name=test-service",
        "ftgo.openapi.version=2.0.0",
        "ftgo.openapi.description=Test API Description"
})
class OpenApiConfigurationTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void openApiBeanIsConfiguredWithApplicationProperties() {
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("test-service API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("2.0.0");
        assertThat(openAPI.getInfo().getDescription()).isEqualTo("Test API Description");
    }

    @Test
    void openApiHasContactInfo() {
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("FTGO Team");
    }

    @Test
    void openApiHasLicenseInfo() {
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Apache 2.0");
    }

    @Test
    void openApiHasDefaultServer() {
        assertThat(openAPI.getServers()).hasSize(1);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/");
    }

    @SpringBootApplication
    static class TestApp {
    }
}
