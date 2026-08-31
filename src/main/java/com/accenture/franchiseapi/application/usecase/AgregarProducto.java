package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.model.exceptions.EntityNotFound;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.domain.port.ProductRepository;
import reactor.core.publisher.Mono;

public class AgregarProducto {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public AgregarProducto(ProductRepository productRepository, BranchRepository branchRepository) {
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    public Mono<Product> ejecutar(String branchId, String nombre, int stock) {
        return branchRepository.findById(branchId)
            .switchIfEmpty(Mono.error(new EntityNotFound("sucursal", branchId)))
            .then(Mono.fromCallable(() -> Product.builder()
                .name(nombre)
                .branchId(branchId)
                .stock(stock)
                .build()))
            .flatMap(productRepository::save);
    }
}
