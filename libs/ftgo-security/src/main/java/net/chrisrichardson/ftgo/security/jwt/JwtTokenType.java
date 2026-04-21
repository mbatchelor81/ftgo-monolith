package net.chrisrichardson.ftgo.security.jwt;

/**
 * Discriminates between the two token shapes FTGO issues.
 *
 * <p>Access tokens are presented on every API call and therefore carry the
 * caller's full authorization profile ({@code roles}, {@code permissions}).
 * Refresh tokens are long-lived, kept server-side by the client, and carry
 * only the identity claims needed to mint a new access token.
 */
public enum JwtTokenType {

    ACCESS("access"),
    REFRESH("refresh");

    private final String claimValue;

    JwtTokenType(String claimValue) {
        this.claimValue = claimValue;
    }

    /** The literal written into the {@code token_type} claim. */
    public String claimValue() {
        return claimValue;
    }

    /** Parses a token-type claim; throws when the value is unknown. */
    public static JwtTokenType fromClaim(String claimValue) {
        for (JwtTokenType type : values()) {
            if (type.claimValue.equals(claimValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown token_type claim: " + claimValue);
    }
}
