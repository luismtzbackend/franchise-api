package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.repository;

import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface FranchiseReactiveMongoRepository extends ReactiveMongoRepository<FranchiseDocument, String> {

    Mono<FranchiseDocument> findByName(String name);
}
