package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.port.FranchiseRepository;
import reactor.core.publisher.Mono;

public class CrearFranquicia {

    private final FranchiseRepository franchiseRepository;

    public CrearFranquicia(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    public Mono<Franchise> ejecutar(String nombre) {
        return Mono.fromCallable(() -> Franchise.builder()
                .name(nombre)
                .build())
            .flatMap(franchiseRepository::save);
    }
}
