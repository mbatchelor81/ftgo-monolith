package com.ftgo.security.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenRefreshServiceTest {

    @Test
    void refreshReturnsEmptyWhenDisabled() {
        FtgoJwtProperties.TokenRefresh refreshProps = new FtgoJwtProperties.TokenRefresh();
        refreshProps.setEnabled(false);

        TokenRefreshService service = new TokenRefreshService(refreshProps);

        assertThat(service.refreshAccessToken("some-token")).isEmpty();
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    void refreshReturnsEmptyWhenNoEndpointConfigured() {
        FtgoJwtProperties.TokenRefresh refreshProps = new FtgoJwtProperties.TokenRefresh();
        refreshProps.setEnabled(true);
        refreshProps.setTokenEndpoint(null);

        TokenRefreshService service = new TokenRefreshService(refreshProps);

        assertThat(service.refreshAccessToken("some-token")).isEmpty();
    }

    @Test
    void refreshBeforeExpirySecondsIsConfigurable() {
        FtgoJwtProperties.TokenRefresh refreshProps = new FtgoJwtProperties.TokenRefresh();
        refreshProps.setRefreshBeforeExpirySeconds(600);

        TokenRefreshService service = new TokenRefreshService(refreshProps);

        assertThat(service.getRefreshBeforeExpirySeconds()).isEqualTo(600);
    }
}
