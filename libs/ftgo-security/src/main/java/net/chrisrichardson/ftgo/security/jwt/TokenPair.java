package net.chrisrichardson.ftgo.security.jwt;

/**
 * The access + refresh tokens minted by a single login (or refresh) call.
 */
public record TokenPair(IssuedToken accessToken, IssuedToken refreshToken) {
}
