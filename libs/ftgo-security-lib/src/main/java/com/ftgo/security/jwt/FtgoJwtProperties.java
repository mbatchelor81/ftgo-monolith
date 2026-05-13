package com.ftgo.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Externalized JWT configuration for FTGO services.
 *
 * <p>Properties are bound from {@code ftgo.security.jwt.*} in application.yml.
 */
@ConfigurationProperties(prefix = "ftgo.security.jwt")
public class FtgoJwtProperties {

    private boolean enabled = false;

    private String issuerUri;

    private String jwkSetUri;

    private String rolesClaimName = "realm_access.roles";

    private String permissionsClaimName = "permissions";

    private String userIdClaimName = "sub";

    private String rolePrefix = "ROLE_";

    private final TokenRefresh tokenRefresh = new TokenRefresh();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getRolesClaimName() {
        return rolesClaimName;
    }

    public void setRolesClaimName(String rolesClaimName) {
        this.rolesClaimName = rolesClaimName;
    }

    public String getPermissionsClaimName() {
        return permissionsClaimName;
    }

    public void setPermissionsClaimName(String permissionsClaimName) {
        this.permissionsClaimName = permissionsClaimName;
    }

    public String getUserIdClaimName() {
        return userIdClaimName;
    }

    public void setUserIdClaimName(String userIdClaimName) {
        this.userIdClaimName = userIdClaimName;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    public TokenRefresh getTokenRefresh() {
        return tokenRefresh;
    }

    public static class TokenRefresh {

        private boolean enabled = false;

        private long refreshBeforeExpirySeconds = 60;

        private String tokenEndpoint;

        private String clientId;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getRefreshBeforeExpirySeconds() {
            return refreshBeforeExpirySeconds;
        }

        public void setRefreshBeforeExpirySeconds(long refreshBeforeExpirySeconds) {
            this.refreshBeforeExpirySeconds = refreshBeforeExpirySeconds;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public void setTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }
}
