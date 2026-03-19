package com.ftgo.common.jpa;

import com.ftgo.common.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/**
 * JPA converter to persist {@link Money} value objects as their BigDecimal amount.
 *
 * <p>Apply via {@code @Convert(converter = MoneyConverter.class)} on entity fields,
 * or register as an auto-apply converter by setting {@code autoApply = true}.</p>
 */
@Converter
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        return money == null ? null : new BigDecimal(money.asString());
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }
}
