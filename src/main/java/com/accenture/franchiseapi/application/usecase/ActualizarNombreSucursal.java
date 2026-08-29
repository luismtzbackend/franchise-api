package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.BranchRepository;
import reactor.core.publisher.Mono;

public class ActualizarNombreSucursal {

    private final BranchRepository branchRepository;

    public ActualizarNombreSucursal(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public Mono<Void> ejecutar(String id, String nuevoNombre) {
        return branchRepository.updateName(id, nuevoNombre)
            .then();
    }
}
