package net.chrisrichardson.ftgo.common.errors;

import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP test for {@link GlobalExceptionHandler} — drives the handler through
 * Spring's {@code MockMvc} with a controller that raises one exception per
 * branch, then asserts each response matches the documented
 * {@link ErrorResponse} shape.
 *
 * <p>Uses {@link MockMvcBuilders#standaloneSetup} so the test exercises the
 * full Spring MVC dispatch pipeline without booting an application context —
 * this keeps the fixture compatible with the JDK 17 sealed-module runtime
 * used by the rest of the build.
 */
public class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new TestController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  public void entityNotFoundReturns404WithErrorCode() throws Exception {
    mockMvc.perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode", is(ErrorCode.ORDER_NOT_FOUND.code())))
        .andExpect(jsonPath("$.status", is(404)))
        .andExpect(jsonPath("$.path", is("/test/not-found")))
        .andExpect(jsonPath("$.timestamp", notNullValue()));
  }

  @Test
  public void conflictExceptionReturns409() throws Exception {
    mockMvc.perform(get("/test/conflict"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode", is(ErrorCode.CONFLICT.code())));
  }

  @Test
  public void businessRuleViolationReturns422() throws Exception {
    mockMvc.perform(get("/test/business-rule"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errorCode", is(ErrorCode.ORDER_MINIMUM_NOT_MET.code())));
  }

  @Test
  public void unsupportedStateTransitionReturns409() throws Exception {
    mockMvc.perform(get("/test/state-transition"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode", is(ErrorCode.ORDER_STATE_INVALID.code())));
  }

  @Test
  public void beanValidationReturns400WithFieldErrors() throws Exception {
    mockMvc.perform(post("/test/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode", is(ErrorCode.VALIDATION_FAILED.code())))
        .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
        .andExpect(jsonPath("$.fieldErrors[0].field", is("name")));
  }

  @Test
  public void downstreamTimeoutReturns503() throws Exception {
    mockMvc.perform(get("/test/downstream-timeout"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.errorCode", is(ErrorCode.SERVICE_UNAVAILABLE.code())));
  }

  @Test
  public void unhandledExceptionReturns500WithoutLeakingDetails() throws Exception {
    mockMvc.perform(get("/test/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.errorCode", is(ErrorCode.INTERNAL_ERROR.code())))
        // The raw exception message ("internal DB password=shh") must NOT
        // reach the client — only the canonical default message is surfaced.
        .andExpect(jsonPath("$.message", is(ErrorCode.INTERNAL_ERROR.defaultMessage())));
  }

  @Test
  public void errorResponseIncludesTraceIdFromMdc() throws Exception {
    MDC.put("traceId", "abc-123");
    try {
      mockMvc.perform(get("/test/not-found"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.traceId", is("abc-123")));
    } finally {
      MDC.remove("traceId");
    }
  }

  // ------------------------------------------------------------------------
  // Test fixtures
  // ------------------------------------------------------------------------

  @RestController
  public static class TestController {

    @GetMapping("/test/not-found")
    public void notFound() {
      throw new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND, "Order 42 not found");
    }

    @GetMapping("/test/conflict")
    public void conflict() {
      throw new ConflictException(ErrorCode.CONFLICT, "Resource version mismatch");
    }

    @GetMapping("/test/business-rule")
    public void businessRule() {
      throw new BusinessRuleViolationException(ErrorCode.ORDER_MINIMUM_NOT_MET,
          "Order total is below minimum");
    }

    @GetMapping("/test/state-transition")
    public void stateTransition() {
      throw new UnsupportedStateTransitionException(null);
    }

    @PostMapping("/test/validate")
    public void validate(@Valid @RequestBody Payload payload) {
      // no-op: reaching this point means validation passed
    }

    @GetMapping("/test/downstream-timeout")
    public void downstreamTimeout() {
      throw new ResourceAccessException("connection timed out");
    }

    @GetMapping("/test/boom")
    public void boom() {
      throw new RuntimeException("internal DB password=shh");
    }
  }

  public static class Payload {
    @NotBlank
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
