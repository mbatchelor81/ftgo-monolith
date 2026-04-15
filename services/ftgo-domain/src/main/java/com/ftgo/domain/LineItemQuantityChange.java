package com.ftgo.domain;

import com.ftgo.common.Money;

public class LineItemQuantityChange {

    private final Money currentOrderTotal;
    private final Money newOrderTotal;
    private final Money delta;

    public LineItemQuantityChange(Money currentOrderTotal, Money newOrderTotal, Money delta) {
        this.currentOrderTotal = currentOrderTotal;
        this.newOrderTotal = newOrderTotal;
        this.delta = delta;
    }

    public Money getCurrentOrderTotal() {
        return currentOrderTotal;
    }

    public Money getNewOrderTotal() {
        return newOrderTotal;
    }

    public Money getDelta() {
        return delta;
    }
}
