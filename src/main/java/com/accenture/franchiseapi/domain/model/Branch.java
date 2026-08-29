package com.accenture.franchiseapi.domain.model;

import java.util.Objects;

public final class Branch {

    private final String id;
    private final String name;
    private final String franchiseId;

    private Branch(Builder builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la sucursal es obligatorio");
        }
        if (builder.franchiseId == null || builder.franchiseId.isBlank()) {
            throw new IllegalArgumentException("La sucursal debe pertenecer a una franquicia");
        }
        this.id = builder.id;
        this.name = builder.name.trim();
        this.franchiseId = builder.franchiseId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .name(this.name)
                .franchiseId(this.franchiseId);
    }

    public Branch rename(String newName) {
        return this.toBuilder().name(newName).build();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFranchiseId() {
        return franchiseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Branch other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Branch{id='" + id + "', name='" + name + "', franchiseId='" + franchiseId + "'}";
    }

    public static final class Builder {

        private String id;
        private String name;
        private String franchiseId;

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

        public Builder franchiseId(String franchiseId) {
            this.franchiseId = franchiseId;
            return this;
        }

        public Branch build() {
            return new Branch(this);
        }
    }
}