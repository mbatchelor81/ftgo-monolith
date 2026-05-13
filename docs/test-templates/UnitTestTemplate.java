package com.ftgo.BOUNDED_CONTEXT.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// =============================================================================
// UNIT TEST TEMPLATE
// =============================================================================
// Copy this template and replace:
//   - BOUNDED_CONTEXT → order, consumer, restaurant, courier
//   - ServiceUnderTest → the service class being tested
//   - DependencyRepository → the repository or dependency being mocked
//   - Entity → the domain entity being tested
//
// Conventions:
//   - File location: src/test/java/com/ftgo/<context>/domain/<Service>Test.java
//   - Naming:        methodName_condition_expectedResult
//   - Structure:     Arrange-Act-Assert (AAA)
//   - Assertions:    AssertJ (assertThat)
//   - Mocking:       Mockito with @ExtendWith(MockitoExtension.class)
// =============================================================================

@ExtendWith(MockitoExtension.class)
class ServiceUnderTestTest {

    @Mock
    private DependencyRepository dependencyRepository;

    @InjectMocks
    private ServiceUnderTest serviceUnderTest;

    @Nested
    @DisplayName("createEntity")
    class CreateEntity {

        @Test
        void createEntity_withValidInput_returnsEntity() {
            // Arrange
            // Entity entity = EntityBuilder.anEntity().build();
            // when(dependencyRepository.save(any())).thenReturn(entity);

            // Act
            // Entity result = serviceUnderTest.createEntity(/* params */);

            // Assert
            // assertThat(result).isNotNull();
            // assertThat(result.getState()).isEqualTo(ExpectedState.PENDING);
            // verify(dependencyRepository).save(any(Entity.class));
        }

        @Test
        void createEntity_withInvalidInput_throwsException() {
            // Arrange & Act & Assert
            // assertThatThrownBy(() -> serviceUnderTest.createEntity(null))
            //     .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("findEntity")
    class FindEntity {

        @Test
        void findById_existingEntity_returnsEntity() {
            // Arrange
            // when(dependencyRepository.findById(1L)).thenReturn(Optional.of(entity));

            // Act
            // Optional<Entity> result = serviceUnderTest.findById(1L);

            // Assert
            // assertThat(result).isPresent();
        }

        @Test
        void findById_nonExistentEntity_returnsEmpty() {
            // Arrange
            // when(dependencyRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            // Optional<Entity> result = serviceUnderTest.findById(999L);

            // Assert
            // assertThat(result).isEmpty();
        }
    }
}
