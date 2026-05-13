package com.ftgo.apigateway.config;

import com.ftgo.security.jwt.FtgoJwtProperties;
import com.ftgo.security.jwt.JwtClaimsExtractor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(FtgoJwtProperties.class)
public class GatewaySecurityConfig {

  private final FtgoJwtProperties jwtProperties;

  public GatewaySecurityConfig(FtgoJwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  @Bean
  public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
    http
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .authorizeExchange(exchanges -> exchanges
        .pathMatchers(
          "/actuator/health",
          "/actuator/health/**",
          "/actuator/info",
          "/actuator/prometheus"
        ).permitAll()
        .anyExchange().authenticated())
      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    JwtClaimsExtractor claimsExtractor = new JwtClaimsExtractor(jwtProperties);
    String rolePrefix = jwtProperties.getRolePrefix();

    return jwt -> {
      List<String> roles = claimsExtractor.extractRoles(jwt);
      List<String> permissions = claimsExtractor.extractPermissions(jwt);

      List<GrantedAuthority> authorities = Stream.<GrantedAuthority>concat(
        roles.stream().map(role -> new SimpleGrantedAuthority(role.startsWith(rolePrefix) ? role : rolePrefix + role)),
        permissions.stream().map(SimpleGrantedAuthority::new)
      ).toList();

      return Mono.just(new JwtAuthenticationToken(jwt, authorities,
        claimsExtractor.extractUserId(jwt)));
    };
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(false);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
