package com.ftgo.restaurant.domain;

import com.ftgo.testlib.base.BaseUnitTest;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.ftgo.testlib.builder.TestBuilders.restaurant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test template for Restaurant domain logic.
 * Demonstrates testing entity creation, menu lookups,
 * and boundary conditions using test builders.
 */
@DisplayName("Restaurant Domain")
class RestaurantDomainUnitTest extends BaseUnitTest {

    @Nested
    @DisplayName("Restaurant creation")
    class Creation {

        @Test
        void createRestaurant_withValidInput_setsProperties() {
            // Arrange & Act
            Restaurant restaurant = restaurant()
                    .withName("Ajanta")
                    .build();

            // Assert
            assertThat(restaurant.getName()).isEqualTo("Ajanta");
        }
    }

    @Nested
    @DisplayName("Menu item lookup")
    class MenuLookup {

        @Test
        void findMenuItem_whenExists_returnsItem() {
            // Arrange
            Restaurant restaurant = restaurant()
                    .clearMenuItems()
                    .withMenuItem("item-1", "Pad Thai", "11.99")
                    .build();

            // Act
            Optional<MenuItem> found = restaurant.findMenuItem("item-1");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Pad Thai");
        }

        @Test
        void findMenuItem_whenNotExists_returnsEmpty() {
            // Arrange
            Restaurant restaurant = restaurant().build();

            // Act
            Optional<MenuItem> found = restaurant.findMenuItem("nonexistent");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Menu revision")
    class MenuRevision {

        @Test
        void reviseMenu_throwsUnsupportedOperation() {
            // Arrange
            Restaurant restaurant = restaurant().build();

            // Act & Assert — reviseMenu is not yet implemented
            assertThatThrownBy(() -> restaurant.reviseMenu(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
