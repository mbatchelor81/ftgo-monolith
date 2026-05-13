package com.ftgo.BOUNDED_CONTEXT.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.mockMvc;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// =============================================================================
// API TEST TEMPLATE
// =============================================================================
// Copy this template and replace:
//   - BOUNDED_CONTEXT → order, consumer, restaurant, courier
//   - Controller → the REST controller being tested
//   - Service → the service dependency
//   - /api/entities → the actual endpoint path
//
// Conventions:
//   - File location: src/test/java/com/ftgo/<context>/web/<Controller>ApiTest.java
//   - Uses @WebMvcTest (loads only web layer — fast)
//   - Uses Rest-Assured MockMvc for fluent assertions
//   - Mocks all service dependencies with @MockBean
//   - Tests HTTP status codes, response body, content type
// =============================================================================

@WebMvcTest(ControllerUnderTest.class)
class ControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ServiceDependency serviceDependency;

    @BeforeEach
    void setUp() {
        mockMvc(mvc);
    }

    @Test
    void getEntity_existingId_returns200WithBody() {
        // Arrange
        // Entity entity = EntityBuilder.anEntity().withId(1L).build();
        // when(serviceDependency.findById(1L)).thenReturn(Optional.of(entity));

        // Act & Assert
        // given()
        // .when()
        //     .get("/api/entities/1")
        // .then()
        //     .statusCode(200)
        //     .body("id", equalTo(1))
        //     .body("state", equalTo("ACTIVE"));
    }

    @Test
    void getEntity_nonExistentId_returns404() {
        // Arrange
        // when(serviceDependency.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        // given()
        // .when()
        //     .get("/api/entities/999")
        // .then()
        //     .statusCode(404);
    }

    @Test
    void createEntity_validRequest_returns201() {
        // Arrange
        // Entity created = EntityBuilder.anEntity().withId(1L).build();
        // when(serviceDependency.create(any())).thenReturn(created);

        // Act & Assert
        // given()
        //     .contentType("application/json")
        //     .body("{\"name\": \"Test\"}")
        // .when()
        //     .post("/api/entities")
        // .then()
        //     .statusCode(201)
        //     .body("id", equalTo(1));
    }

    @Test
    void createEntity_invalidRequest_returns400() {
        // Act & Assert
        // given()
        //     .contentType("application/json")
        //     .body("{}")
        // .when()
        //     .post("/api/entities")
        // .then()
        //     .statusCode(400);
    }
}
