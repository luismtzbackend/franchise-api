package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.mapper;

import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document.FranchiseDocument;
import org.springframework.stereotype.Component;

@Component
public class FranchiseDocumentMapper {

    public FranchiseDocument toDocument(Franchise franchise) {
        return new FranchiseDocument(
            franchise.getId(),
            franchise.getName()
        );
    }

    public Franchise toDomain(FranchiseDocument document) {
        return Franchise.builder()
            .id(document.getId())
            .name(document.getName())
            .build();
    }
}
