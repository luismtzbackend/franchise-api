package com.accenture.franchiseapi.domain.model;

import java.util.Objects;

public final class Franchise {

    private final String id;
    private final String name;

    private Franchise(Builder builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la franquicia es obligatorio");
        }
        this.id = builder.id;
        this.name = builder.name.trim();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .name(this.name);
    }

    public Franchise rename(String newName) {
        return this.toBuilder().name(newName).build();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Franchise other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Franchise{id='" + id + "', name='" + name + "'}";
    }

    public static final class Builder {

        private String id;
        private String name;

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

        public Franchise build() {
            return new Franchise(this);
        }
    }
}