package com.ftgo.security.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RequestUtils}.
 */
class RequestUtilsTest {

    @Test
    @DisplayName("extractBearerToken returns token when valid Bearer header present")
    void extractBearerToken_validHeader_returnsToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc123token");

        Optional<String> token = RequestUtils.extractBearerToken(request);

        assertThat(token).isPresent().hasValue("abc123token");
    }

    @Test
    @DisplayName("extractBearerToken returns empty when no Authorization header")
    void extractBearerToken_noHeader_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Optional<String> token = RequestUtils.extractBearerToken(request);

        assertThat(token).isEmpty();
    }

    @Test
    @DisplayName("extractBearerToken returns empty when non-Bearer auth scheme")
    void extractBearerToken_basicAuth_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        Optional<String> token = RequestUtils.extractBearerToken(request);

        assertThat(token).isEmpty();
    }

    @Test
    @DisplayName("extractBearerToken returns empty when Bearer prefix with empty token")
    void extractBearerToken_emptyToken_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");

        Optional<String> token = RequestUtils.extractBearerToken(request);

        assertThat(token).isEmpty();
    }

    @Test
    @DisplayName("getClientIpAddress returns X-Forwarded-For when present")
    void getClientIpAddress_withXForwardedFor_returnsFirstIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("127.0.0.1");

        String ip = RequestUtils.getClientIpAddress(request);

        assertThat(ip).isEqualTo("203.0.113.50");
    }

    @Test
    @DisplayName("getClientIpAddress returns remote address when no forwarded header")
    void getClientIpAddress_noForwardedHeader_returnsRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");

        String ip = RequestUtils.getClientIpAddress(request);

        assertThat(ip).isEqualTo("192.168.1.100");
    }
}
