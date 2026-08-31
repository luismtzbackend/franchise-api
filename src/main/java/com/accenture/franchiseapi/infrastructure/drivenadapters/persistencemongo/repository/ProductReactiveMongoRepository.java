package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.repository;

import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.document.ProductDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductReactiveMongoRepository extends ReactiveMongoRepository<ProductDocument, String> {

    Flux<ProductDocument> findByBranchId(String branchId);

    Mono<ProductDocument> findTopByBranchIdOrderByStockDesc(String branchId);

    Flux<ProductDocument> findByIdIn(java.util.List<String> ids);

    Flux<ProductDocument> findByBranchIdIn(java.util.List<String> branchIds);
}
