package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

public class ActualizarNombreProducto {

    private final ProductRepository productRepository;

    public ActualizarNombreProducto(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<Void> ejecutar(String productId, String nuevoNombre) {
        return productRepository.updateName(productId, nuevoNombre)
            .then();
    }
}
