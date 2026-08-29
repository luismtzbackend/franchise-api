package com.accenture.franchiseapi.domain.model;

import com.accenture.franchiseapi.domain.model.exceptions.InvalidStock;
import java.util.Objects;

public final class Stock {

    private final int value;

    private Stock(int value) {
        if (value < 0) {
            throw new InvalidStock(value);
        }
        this.value = value;
    }

    public static Stock of(int value) {
        return new Stock(value);
    }

    public int value() {
        return value;
    }

    public boolean isGreaterThan(Stock other) {
        return this.value > other.value;
    }

    public boolean isLessThan(Stock other) {
        return this.value < other.value;
    }

    public Stock add(int quantity) {
        return Stock.of(this.value + quantity);
    }

    public Stock subtract(int quantity) {
        return Stock.of(this.value - quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock stock)) return false;
        return value == stock.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
