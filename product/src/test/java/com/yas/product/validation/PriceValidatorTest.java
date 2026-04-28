package com.yas.product.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PriceValidatorTest {

    private final PriceValidator priceValidator = new PriceValidator();

    @Test
    void isValid_whenPriceIsZeroOrPositive_thenReturnTrue() {
        assertTrue(priceValidator.isValid(0D, null));
        assertTrue(priceValidator.isValid(10.5D, null));
    }

    @Test
    void isValid_whenPriceIsNegative_thenReturnFalse() {
        assertFalse(priceValidator.isValid(-1D, null));
    }

    @Test
    void isValid_whenPriceIsNull_thenThrowException() {
        assertThrows(NullPointerException.class, () -> priceValidator.isValid(null, null));
    }
}
