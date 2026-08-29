package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

public class   EliminarProducto {

    private final ProductRepository productRepository;

    public EliminarProducto(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<Void> ejecutar(String productId) {
        return productRepository.deleteById(productId);
    }
}
