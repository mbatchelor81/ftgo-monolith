package com.ftgo.apigateway;

import com.ftgo.apigateway.config.CircuitBreakerConfig;
import com.ftgo.apigateway.config.FallbackController;
import com.ftgo.apigateway.config.GatewayRoutingConfig;
import com.ftgo.apigateway.config.GatewaySecurityConfig;
import com.ftgo.apigateway.filter.RequestResponseLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {
    "ftgo.gateway.rate-limiting.enabled=false",
    "ftgo.security.jwt.enabled=true",
    "ftgo.security.jwt.jwk-set-uri=https://localhost/.well-known/jwks.json",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://localhost/.well-known/jwks.json"
  }
)
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

  @Autowired
  private ApplicationContext context;

  @Test
  void contextLoads_always_startsSuccessfully() {
    assertThat(context).isNotNull();
  }

  @Test
  void securityConfig_whenContextLoaded_beanExists() {
    assertThat(context.getBean(GatewaySecurityConfig.class)).isNotNull();
  }

  @Test
  void routeLocator_whenContextLoaded_beanExists() {
    assertThat(context.getBean(RouteLocator.class)).isNotNull();
  }

  @Test
  void loggingFilter_whenContextLoaded_beanExists() {
    assertThat(context.getBean(RequestResponseLoggingFilter.class)).isNotNull();
  }

  @Test
  void fallbackController_whenContextLoaded_beanExists() {
    assertThat(context.getBean(FallbackController.class)).isNotNull();
  }

  @Test
  void circuitBreakerConfig_whenContextLoaded_beanExists() {
    assertThat(context.getBean(CircuitBreakerConfig.class)).isNotNull();
  }

  @Test
  void routingConfig_whenContextLoaded_beanExists() {
    assertThat(context.getBean(GatewayRoutingConfig.class)).isNotNull();
  }
}
