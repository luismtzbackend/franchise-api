package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

public class ActualizarStockProducto {

    private final ProductRepository productRepository;

    public ActualizarStockProducto(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<Void> ejecutar(String productId, int nuevoStock) {
        if (nuevoStock < 0) {
            return Mono.error(new IllegalArgumentException("El stock no puede ser negativo"));
        }
        return productRepository.updateStock(productId, nuevoStock)
            .then();
    }
}
