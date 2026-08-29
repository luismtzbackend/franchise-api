package com.accenture.franchiseapi.domain.port;

import java.util.List;
import reactor.core.publisher.Mono;

public interface TopStockCache {

    Mono<List<String>> get(String franchiseId);

    Mono<Void> put(String franchiseId, List<String> productIds);

    Mono<Void> evict(String franchiseId);
}
