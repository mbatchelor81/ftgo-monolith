package net.chrisrichardson.ftgo.testlib.builders;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Restaurant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantBuilderTest {

    @Test
    void aRestaurant_withDefaults_createsRestaurant() {
        Restaurant restaurant = RestaurantBuilder.aRestaurant().build();

        assertThat(restaurant).isNotNull();
        assertThat(restaurant.getId()).isEqualTo(1L);
        assertThat(restaurant.getName()).isEqualTo("Test Restaurant");
    }

    @Test
    void aRestaurant_withCustomValues_setsFieldsCorrectly() {
        Restaurant restaurant = RestaurantBuilder.aRestaurant()
                .withId(5L)
                .withName("Ajanta")
                .withMenuItem("1", "Chicken Vindaloo", new Money("12.34"))
                .build();

        assertThat(restaurant.getId()).isEqualTo(5L);
        assertThat(restaurant.getName()).isEqualTo("Ajanta");
        assertThat(restaurant.findMenuItem("1")).isPresent();
    }
}
