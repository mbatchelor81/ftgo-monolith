package com.ftgo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ftgo.common.Address;
import com.ftgo.common.Money;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Restaurant} entity.
 *
 * <p>Demonstrates testing of:
 *
 * <ul>
 *   <li>Restaurant construction with menu
 *   <li>Menu item lookup
 *   <li>Unsupported operations
 * </ul>
 */
@DisplayName("Restaurant")
class RestaurantTest {

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        var menu =
                new RestaurantMenu(
                        List.of(
                                new MenuItem("item-1", "Chicken Vindaloo", new Money("12.34")),
                                new MenuItem("item-2", "Lamb Biryani", new Money("15.99"))));
        restaurant =
                new Restaurant(
                        "Ajanta", new Address("1 Main St", null, "Oakland", "CA", "94609"), menu);
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("should create restaurant with name and address")
        void constructor_withNameAndAddress_createsRestaurant() {
            assertThat(restaurant.getName()).isEqualTo("Ajanta");
            assertThat(restaurant.getAddress()).isNotNull();
            assertThat(restaurant.getAddress().getCity()).isEqualTo("Oakland");
        }
    }

    @Nested
    @DisplayName("findMenuItem")
    class FindMenuItem {

        @Test
        @DisplayName("should find menu item by ID when it exists")
        void findMenuItem_whenExists_returnsItem() {
            Optional<MenuItem> found = restaurant.findMenuItem("item-1");

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Chicken Vindaloo");
            assertThat(found.get().getPrice()).isEqualTo(new Money("12.34"));
        }

        @Test
        @DisplayName("should return empty when menu item does not exist")
        void findMenuItem_whenNotExists_returnsEmpty() {
            Optional<MenuItem> found = restaurant.findMenuItem("nonexistent");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("reviseMenu")
    class ReviseMenu {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void reviseMenu_shouldThrowUnsupported() {
            var newMenu = new RestaurantMenu(List.of());

            assertThatThrownBy(() -> restaurant.reviseMenu(newMenu))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
