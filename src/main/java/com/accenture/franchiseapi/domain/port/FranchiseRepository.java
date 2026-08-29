package com.accenture.franchiseapi.domain.port;

import com.accenture.franchiseapi.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);

    Mono<Franchise> updateName(String id, String name);
}
