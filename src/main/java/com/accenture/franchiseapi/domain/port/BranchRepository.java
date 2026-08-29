package com.accenture.franchiseapi.domain.port;

import com.accenture.franchiseapi.domain.model.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> save(Branch branch);

    Mono<Branch> findById(String id);

    Flux<Branch> findByFranchiseId(String franchiseId);

    Mono<Branch> updateName(String id, String name);
}
