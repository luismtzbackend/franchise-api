package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.repository;

import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document.BranchDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BranchReactiveMongoRepository extends ReactiveMongoRepository<BranchDocument, String> {

    Flux<BranchDocument> findByFranchiseId(String franchiseId);
}
