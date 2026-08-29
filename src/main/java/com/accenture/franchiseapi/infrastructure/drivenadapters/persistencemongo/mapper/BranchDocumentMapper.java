package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.mapper;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document.BranchDocument;
import org.springframework.stereotype.Component;

@Component
public class BranchDocumentMapper {

    public BranchDocument toDocument(Branch branch) {
        return new BranchDocument(
            branch.getId(),
            branch.getName(),
            branch.getFranchiseId()
        );
    }

    public Branch toDomain(BranchDocument document) {
        return Branch.builder()
            .id(document.getId())
            .name(document.getName())
            .franchiseId(document.getFranchiseId())
            .build();
    }
}
