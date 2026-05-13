package com.ftgo.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Handles JWT token refresh via the OAuth2 token endpoint using the
 * {@code refresh_token} grant type.
 *
 * <p>When enabled, this service exchanges a refresh token for a new
 * access token before the current one expires.
 */
public class TokenRefreshService {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FtgoJwtProperties.TokenRefresh refreshProperties;
    private final HttpClient httpClient;

    public TokenRefreshService(FtgoJwtProperties.TokenRefresh refreshProperties) {
        this.refreshProperties = refreshProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    TokenRefreshService(FtgoJwtProperties.TokenRefresh refreshProperties, HttpClient httpClient) {
        this.refreshProperties = refreshProperties;
        this.httpClient = httpClient;
    }

    /**
     * Exchanges a refresh token for a new access token.
     *
     * @param refreshToken the OAuth2 refresh token
     * @return the new access token, or empty if the refresh failed
     */
    public Optional<String> refreshAccessToken(String refreshToken) {
        if (!refreshProperties.isEnabled()) {
            return Optional.empty();
        }

        String tokenEndpoint = refreshProperties.getTokenEndpoint();
        if (tokenEndpoint == null || tokenEndpoint.isBlank()) {
            log.warn("Token refresh is enabled but no token endpoint is configured");
            return Optional.empty();
        }

        String clientId = refreshProperties.getClientId();
        if (clientId == null || clientId.isBlank()) {
            log.warn("Token refresh is enabled but no client-id is configured");
            return Optional.empty();
        }

        String formBody = "grant_type=" + encode("refresh_token")
                + "&refresh_token=" + encode(refreshToken)
                + "&client_id=" + encode(refreshProperties.getClientId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode json = OBJECT_MAPPER.readTree(response.body());
                String accessToken = json.path("access_token").asText(null);
                if (accessToken != null) {
                    log.debug("Successfully refreshed access token");
                    return Optional.of(accessToken);
                }
            }
            log.warn("Token refresh failed with status {}", response.statusCode());
        } catch (IOException | InterruptedException e) {
            log.error("Error during token refresh", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return Optional.empty();
    }

    public long getRefreshBeforeExpirySeconds() {
        return refreshProperties.getRefreshBeforeExpirySeconds();
    }

    public boolean isEnabled() {
        return refreshProperties.isEnabled();
    }

    private static String encode(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
