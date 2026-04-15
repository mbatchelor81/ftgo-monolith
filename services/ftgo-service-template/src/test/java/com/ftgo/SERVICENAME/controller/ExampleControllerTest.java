package com.ftgo.SERVICENAME.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Controller test template using @WebMvcTest (slice test).
 *
 * <p>Controller tests should:
 *
 * <ul>
 *   <li>Use @WebMvcTest for a thin Spring context (no DB, no external deps)
 *   <li>Mock service-layer dependencies with @MockBean
 *   <li>Verify HTTP status codes, response bodies, and content types
 *   <li>Test input validation (e.g., @Valid on request DTOs)
 * </ul>
 *
 * <p>Replace SERVICENAME with the actual service name when copying this template.
 */
// @WebMvcTest(ExampleController.class)  // Uncomment once a real controller exists
@DisplayName("ExampleController")
class ExampleControllerTest {

    // @Autowired
    // private MockMvc mockMvc;

    // @MockBean
    // private ExampleService exampleService;

    @Test
    @DisplayName("GET /api/example should return 200")
    void getExample_shouldReturn200() throws Exception {
        // mockMvc.perform(get("/api/example"))
        //     .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/example/{id} with unknown id should return 404")
    void getExampleById_notFound_shouldReturn404() throws Exception {
        // when(exampleService.findById(999L)).thenReturn(Optional.empty());
        //
        // mockMvc.perform(get("/api/example/999"))
        //     .andExpect(status().isNotFound());
    }
}
