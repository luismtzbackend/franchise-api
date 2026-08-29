package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "franchises")
public class FranchiseDocument {

    @Id
    private String id;
    private String name;

    public FranchiseDocument() {
    }

    public FranchiseDocument(String id, String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FranchiseDocument other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "FranchiseDocument{id='" + id + "', name='" + name + "'}";
    }
}
