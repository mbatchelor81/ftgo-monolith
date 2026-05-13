package net.chrisrichardson.ftgo.logging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoggedAspectTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    FtgoLoggingAutoConfiguration.class,
                    AopAutoConfiguration.class
            ))
            .withUserConfiguration(TestServiceConfig.class);

    @Test
    void loggedMethodExecutesSuccessfully() {
        contextRunner.run(context -> {
            TestService service = context.getBean(TestService.class);
            String result = service.greet("world");
            assertThat(result).isEqualTo("Hello, world");
        });
    }

    @Test
    void loggedMethodPropagatesException() {
        contextRunner.run(context -> {
            TestService service = context.getBean(TestService.class);
            assertThatThrownBy(() -> service.fail())
                    .isInstanceOf(IllegalStateException.class);
        });
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestServiceConfig {
        @Bean
        TestService testService() {
            return new TestService();
        }
    }

    static class TestService {
        @Logged
        public String greet(String name) {
            return "Hello, " + name;
        }

        @Logged("failOperation")
        public void fail() {
            throw new IllegalStateException("test error");
        }
    }
}
