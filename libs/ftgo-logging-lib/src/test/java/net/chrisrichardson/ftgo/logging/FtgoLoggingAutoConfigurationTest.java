package net.chrisrichardson.ftgo.logging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoLoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoLoggingAutoConfiguration.class));

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoLoggingAutoConfiguration.class));

    @Test
    void autoConfigurationRegistersLoggingBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LoggingEnvironmentPostProcessor.class);
        });
    }

    @Test
    void disabledWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("ftgo.logging.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LoggingEnvironmentPostProcessor.class);
                });
    }

    @Test
    void webContextRegistersFilters() {
        webContextRunner.run(context -> {
            assertThat(context).getBeanNames(FilterRegistrationBean.class)
                    .contains("correlationIdFilter", "serviceContextFilter");
        });
    }

    @Test
    void correlationIdFilterDisabledByProperty() {
        webContextRunner
                .withPropertyValues("ftgo.logging.correlation-id-enabled=false")
                .run(context -> {
                    String[] filterBeanNames = context.getBeanNamesForType(FilterRegistrationBean.class);
                    assertThat(filterBeanNames).doesNotContain("correlationIdFilter");
                    assertThat(filterBeanNames).contains("serviceContextFilter");
                });
    }

    @Test
    void customServiceNameIsApplied() {
        contextRunner
                .withPropertyValues("ftgo.logging.service-name=order-service")
                .run(context -> {
                    assertThat(context).hasSingleBean(FtgoLoggingProperties.class);
                    FtgoLoggingProperties props = context.getBean(FtgoLoggingProperties.class);
                    assertThat(props.getServiceName()).isEqualTo("order-service");
                });
    }
}
