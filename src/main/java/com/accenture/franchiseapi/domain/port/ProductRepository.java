package com.accenture.franchiseapi.domain.port;

import com.accenture.franchiseapi.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    Mono<Void> deleteById(String id);

    Flux<Product> findByBranchId(String branchId);

    Mono<Product> findTopStockByBranchId(String branchId);

    Mono<Product> updateStock(String id, int newStock);

    Mono<Product> updateName(String id, String name);

    Flux<Product> findByBranchIdIn(java.util.List<String> branchIds);

    Flux<Product> findByIdIn(java.util.List<String> ids);
}
