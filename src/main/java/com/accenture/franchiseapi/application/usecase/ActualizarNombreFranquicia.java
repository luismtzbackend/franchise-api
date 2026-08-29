package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.FranchiseRepository;
import reactor.core.publisher.Mono;

public class ActualizarNombreFranquicia {

    private final FranchiseRepository franchiseRepository;

    public ActualizarNombreFranquicia(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    public Mono<Void> ejecutar(String id, String nuevoNombre) {
        return franchiseRepository.updateName(id, nuevoNombre)
            .then();
    }
}
