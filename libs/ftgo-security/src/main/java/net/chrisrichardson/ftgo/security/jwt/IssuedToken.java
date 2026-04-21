package net.chrisrichardson.ftgo.security.jwt;

import java.time.Instant;

/**
 * A freshly minted JWT together with the metadata a caller typically needs
 * to hand back to the client.
 */
public record IssuedToken(String value, JwtTokenType type, Instant issuedAt, Instant expiresAt) {
}
