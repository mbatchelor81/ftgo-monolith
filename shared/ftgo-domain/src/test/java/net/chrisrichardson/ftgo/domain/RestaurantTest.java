package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantTest {

  @Test
  void constructor_setsNameAndAddress() {
    Address address = new Address("123 Main St", null, "Springfield", "IL", "62701");
    RestaurantMenu menu = new RestaurantMenu(List.of(
        new MenuItem("1", "Burger", new Money("8.99"))
    ));
    Restaurant restaurant = new Restaurant("Test Diner", address, menu);

    assertThat(restaurant.getName()).isEqualTo("Test Diner");
    assertThat(restaurant.getAddress().getCity()).isEqualTo("Springfield");
  }

  @Test
  void findMenuItem_existingItem_returnsItem() {
    RestaurantMenu menu = new RestaurantMenu(List.of(
        new MenuItem("item-1", "Burger", new Money("8.99")),
        new MenuItem("item-2", "Fries", new Money("3.99"))
    ));
    Restaurant restaurant = new Restaurant("Test Diner", null, menu);

    Optional<MenuItem> found = restaurant.findMenuItem("item-2");

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Fries");
  }

  @Test
  void findMenuItem_nonExistingItem_returnsEmpty() {
    RestaurantMenu menu = new RestaurantMenu(List.of(
        new MenuItem("item-1", "Burger", new Money("8.99"))
    ));
    Restaurant restaurant = new Restaurant("Test Diner", null, menu);

    Optional<MenuItem> found = restaurant.findMenuItem("item-999");

    assertThat(found).isEmpty();
  }
}
