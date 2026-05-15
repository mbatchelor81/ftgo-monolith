package net.chrisrichardson.ftgo.testlib.assertions;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.testlib.builders.OrderBuilder;
import org.junit.jupiter.api.Test;

import static net.chrisrichardson.ftgo.testlib.assertions.FtgoAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FtgoAssertionsTest {

    @Test
    void assertThat_order_hasState() {
        Order order = OrderBuilder.anOrder().build();

        assertThat(order).hasState(OrderState.APPROVED);
        assertThat(order).isApproved();
    }

    @Test
    void assertThat_order_hasConsumerId() {
        Order order = OrderBuilder.anOrder()
                .withConsumerId(42L)
                .build();

        assertThat(order).hasConsumerId(42L);
    }

    @Test
    void assertThat_order_wrongState_fails() {
        Order order = OrderBuilder.anOrder().build();

        assertThatThrownBy(() -> assertThat(order).isCancelled())
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void assertThat_money_isPositive() {
        Money money = new Money("10.00");

        assertThat(money).isPositive();
    }

    @Test
    void assertThat_money_isZero() {
        assertThat(Money.ZERO).isZero();
    }

    @Test
    void assertThat_money_isGreaterThanOrEqualTo() {
        Money money = new Money("15.00");

        assertThat(money).isGreaterThanOrEqualTo(new Money("10.00"));
    }

    @Test
    void assertThat_money_equalsString() {
        Money money = new Money("12.34");

        assertThat(money).isEqualTo("12.34");
    }
}
