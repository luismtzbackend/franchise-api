package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "products")
public class ProductDocument {

    @Id
    private String id;
    private String name;
    private String branchId;
    private int stock;

    public ProductDocument() {
    }

    public ProductDocument(String id, String name, String branchId, int stock) {
        this.id = id;
        this.name = name;
        this.branchId = branchId;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductDocument other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ProductDocument{id='" + id + "', name='" + name + "', branchId='" + branchId + "', stock=" + stock + "}";
    }
}
