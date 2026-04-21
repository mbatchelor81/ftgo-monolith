package com.ftgo.restaurant.examples;

import com.ftgo.test.builders.RestaurantBuilder;
import com.ftgo.test.fixtures.FtgoMothers;
import com.ftgo.test.fixtures.RestaurantFixture;
import net.chrisrichardson.ftgo.common.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-tier example for the Restaurant bounded context.
 *
 * <p>Restaurants are the curious case where a "builder" is really a
 * builder-of-a-menu: a restaurant without menu items is degenerate. The
 * example below shows how to assemble a restaurant with one and many
 * menu items, and how to tap into {@link FtgoMothers#ajantaRestaurant()}
 * for the canonical "Ajanta" fixture carried over from the monolith
 * regression suite.
 */
class RestaurantFixtureExampleTest {

    @Test
    void aRestaurant_withDefaults_producesEmptyAjantaMenu() {
        // The fluent builder starts from an empty menu so callers never
        // get a "surprise" menu item — you add exactly what the test
        // needs. Defaults are reserved for identity fields (id, name,
        // city).
        RestaurantFixture restaurant = RestaurantBuilder.aRestaurant().build();

        assertThat(restaurant.id()).isEqualTo(FtgoMothers.AJANTA_ID);
        assertThat(restaurant.name()).isEqualTo(FtgoMothers.AJANTA_NAME);
        assertThat(restaurant.menu()).isEmpty();
    }

    @Test
    void aRestaurant_withMultipleMenuItems_retainsInsertionOrder() {
        RestaurantFixture restaurant = RestaurantBuilder.aRestaurant()
                .withName("Palace of India")
                .withCity("Mountain View")
                .withMenuItem("vindaloo", "Chicken Vindaloo", new Money("12.50"))
                .withMenuItem("naan",     "Garlic Naan",       new Money("3.75"))
                .withMenuItem("rice",     "Basmati Rice",      new Money("4.00"))
                .build();

        assertThat(restaurant.menu())
                .extracting("id")
                .containsExactly("vindaloo", "naan", "rice");
    }

    @Test
    void ajantaRestaurant_exposesChickenVindalooPrice() {
        RestaurantFixture restaurant = FtgoMothers.ajantaRestaurant().build();

        assertThat(restaurant.menu()).hasSize(1);
        assertThat(restaurant.menu().get(0).price())
                .isEqualTo(FtgoMothers.CHICKEN_VINDALOO_PRICE);
    }
}
