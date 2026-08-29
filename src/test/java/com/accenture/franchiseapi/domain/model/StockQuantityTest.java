package com.accenture.franchiseapi.domain.model;

import com.accenture.franchiseapi.domain.model.exceptions.InvalidStock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockQuantityTest {

    @Test
    void shouldCreateStockWithValidValue() {
        Stock stock = Stock.of(120);

        assertEquals(120, stock.value());
    }

    @Test
    void shouldAllowZeroStock() {
        Stock stock = Stock.of(0);

        assertEquals(0, stock.value());
    }

    @Test
    void shouldFailWhenStockIsNegative() {
        InvalidStock exception =
                assertThrows(InvalidStock.class, () -> Stock.of(-5));

        assertEquals(-5, exception.getAttemptedValue());
    }

    @Test
    void shouldCompareStockQuantities() {
        Stock higher = Stock.of(10);
        Stock lower = Stock.of(3);

        assertTrue(higher.isGreaterThan(lower));
        assertFalse(lower.isGreaterThan(higher));
    }

    @Test
    void shouldBeEqualByValue() {
        assertEquals(Stock.of(7), Stock.of(7));
    }
}
