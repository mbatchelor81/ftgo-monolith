package com.ftgo.courier.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reference implementation of an FTGO REST endpoint that follows the
 * standards documented in {@code docs/rest-api-standards.md}. Serves as
 * the canonical "hello world" example for new Courier Service controllers.
 */
@RestController
@RequestMapping("/api/v1/service-info")
@Tag(
    name = "Service Info",
    description = "Metadata about the running Courier Service instance."
)
public class ServiceInfoController {

  private final String applicationName;
  private final String apiVersion;

  public ServiceInfoController(
      @Value("${spring.application.name:courier-service}") String applicationName,
      @Value("${ftgo.openapi.version:1.0.0}") String apiVersion) {
    this.applicationName = applicationName;
    this.apiVersion = apiVersion;
  }

  @Operation(
      summary = "Get service info",
      description = "Returns the running service's application name and the "
          + "semver API contract version currently exposed under /api/v1."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Service info returned successfully",
          content = @Content(schema = @Schema(implementation = ServiceInfoResponse.class))
      )
  })
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ServiceInfoResponse getServiceInfo() {
    return new ServiceInfoResponse(applicationName, apiVersion);
  }

  @Schema(description = "Service identification and API contract version.")
  public static class ServiceInfoResponse {

    @Schema(description = "Spring application name", example = "courier-service")
    private final String name;

    @Schema(description = "Semver API contract version exposed under /api/v1", example = "1.0.0")
    private final String apiVersion;

    public ServiceInfoResponse(String name, String apiVersion) {
      this.name = name;
      this.apiVersion = apiVersion;
    }

    public String getName() {
      return name;
    }

    public String getApiVersion() {
      return apiVersion;
    }
  }
}
