package com.ftgo.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

  private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

  private static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final String START_TIME_ATTR = "startTime";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString();
    }

    ServerHttpRequest mutatedRequest = request.mutate()
      .header(REQUEST_ID_HEADER, requestId)
      .build();

    exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

    String method = request.getMethod().name();
    String path = request.getURI().getPath();
    String remoteAddr = request.getRemoteAddress() != null
      && request.getRemoteAddress().getAddress() != null
      ? request.getRemoteAddress().getAddress().getHostAddress()
      : "unknown";

    final String reqId = requestId;
    log.info(">>> {} {} from={} requestId={}", method, path, remoteAddr, reqId);

    return chain.filter(exchange.mutate().request(mutatedRequest).build())
      .then(Mono.fromRunnable(() -> {
        ServerHttpResponse response = exchange.getResponse();
        Long startTime = exchange.getAttribute(START_TIME_ATTR);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

        int statusCode = response.getStatusCode() != null
          ? response.getStatusCode().value()
          : 0;

        log.info("<<< {} {} status={} duration={}ms requestId={}",
          method, path, statusCode, duration, reqId);
      }));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
