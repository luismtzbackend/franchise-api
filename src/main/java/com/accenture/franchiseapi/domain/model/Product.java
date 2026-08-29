package com.accenture.franchiseapi.domain.model;

import java.util.Objects;

public final class Product {

    private final String id;
    private final String name;
    private final String branchId;
    private final Stock stock;


    private Product(Builder builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (builder.branchId == null || builder.branchId.isBlank()) {
            throw new IllegalArgumentException("El producto debe pertenecer a una sucursal");
        }
        this.id = builder.id;
        this.name = builder.name.trim();
        this.branchId = builder.branchId;
        this.stock = builder.stock != null ? builder.stock : Stock.of(0);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .name(this.name)
                .branchId(this.branchId)
                .stock(this.stock);
    }

    public Product rename(String newName) {
        return this.toBuilder().name(newName).build();
    }

    public Product updateStock(int newStock) {
        return this.toBuilder().stock(Stock.of(newStock)).build();
    }

    public boolean hasMoreStockThan(Product other) {
        return this.stock.isGreaterThan(other.stock);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBranchId() {
        return branchId;
    }

    public Stock getStock() {
        return stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name + "', branchId='" + branchId
                + "', stock=" + stock.value() + "}";
    }

    public static final class Builder {

        private String id;
        private String name;
        private String branchId;
        private Stock stock;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder branchId(String branchId) {
            this.branchId = branchId;
            return this;
        }

        public Builder stock(Stock stock) {
            this.stock = stock;
            return this;
        }

        public Builder stock(int stock) {
            this.stock = Stock.of(stock);
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}