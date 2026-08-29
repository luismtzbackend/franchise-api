package com.accenture.franchiseapi.domain.model;

import com.accenture.franchiseapi.domain.model.exceptions.InvalidStock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

    @Test
    void shouldBuildValidProduct() {
        Product product = Product.builder()
                .id("p-1")
                .branchId("b-1")
                .name("Café 500g")
                .stock(120)
                .build();

        assertEquals("p-1", product.getId());
        assertEquals("b-1", product.getBranchId());
        assertEquals("Café 500g", product.getName());
        assertEquals(120, product.getStock().value());
    }

    @Test
    void shouldDefaultStockToZeroWhenNotProvided() {
        Product product = Product.builder()
                .id("p-1")
                .branchId("b-1")
                .name("Café 500g")
                .build();

        assertEquals(0, product.getStock().value());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Product.Builder builder = Product.builder().id("p-1").branchId("b-1").name("  ");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldFailWhenBranchIdIsMissing() {
        Product.Builder builder = Product.builder().id("p-1").name("Café 500g");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldFailWhenStockIsNegative() {
        Product.Builder builder = Product.builder().id("p-1").branchId("b-1").name("Café 500g");

        assertThrows(InvalidStock.class, () -> builder.stock(-1));
    }

    @Test
    void shouldUpdateStockByCreatingNewInstance() {
        Product original = Product.builder()
                .id("p-1")
                .branchId("b-1")
                .name("Café 500g")
                .stock(10)
                .build();

        Product updated = original.updateStock(25);

        assertEquals(25, updated.getStock().value());
        assertEquals(10, original.getStock().value());
    }

    @Test
    void shouldFailWhenUpdatingStockToNegative() {
        Product product = Product.builder()
                .id("p-1")
                .branchId("b-1")
                .name("Café 500g")
                .stock(10)
                .build();

        assertThrows(InvalidStock.class, () -> product.updateStock(-3));
    }

    @Test
    void shouldCompareStockBetweenProducts() {
        Product higher = Product.builder().id("p-1").branchId("b-1").name("A").stock(50).build();
        Product lower = Product.builder().id("p-2").branchId("b-1").name("B").stock(20).build();

        assertTrue(higher.hasMoreStockThan(lower));
    }
}
