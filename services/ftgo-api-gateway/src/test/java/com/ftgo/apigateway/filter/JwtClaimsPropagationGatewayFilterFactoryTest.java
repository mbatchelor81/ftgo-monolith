package com.ftgo.apigateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;

/** Unit tests for {@link JwtClaimsPropagationGatewayFilterFactory}. */
class JwtClaimsPropagationGatewayFilterFactoryTest {

    private final JwtClaimsPropagationGatewayFilterFactory factory =
            new JwtClaimsPropagationGatewayFilterFactory();

    @Test
    void apply_returnsNonNullFilter() {
        GatewayFilter filter = factory.apply(new JwtClaimsPropagationGatewayFilterFactory.Config());
        assertThat(filter).isNotNull();
    }

    @Test
    void headerConstants_areCorrect() {
        assertThat(JwtClaimsPropagationGatewayFilterFactory.HEADER_USER_ID).isEqualTo("X-User-Id");
        assertThat(JwtClaimsPropagationGatewayFilterFactory.HEADER_USER_NAME)
                .isEqualTo("X-User-Name");
        assertThat(JwtClaimsPropagationGatewayFilterFactory.HEADER_USER_ROLES)
                .isEqualTo("X-User-Roles");
        assertThat(JwtClaimsPropagationGatewayFilterFactory.HEADER_USER_PERMISSIONS)
                .isEqualTo("X-User-Permissions");
    }
}
