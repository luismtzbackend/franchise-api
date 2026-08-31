package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.exceptions.EntityNotFound;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.domain.port.FranchiseRepository;
import reactor.core.publisher.Mono;

public class AgregarSucursal {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public AgregarSucursal(BranchRepository branchRepository, FranchiseRepository franchiseRepository) {
        this.branchRepository = branchRepository;
        this.franchiseRepository = franchiseRepository;
    }

    public Mono<Branch> ejecutar(String franchiseId, String nombre) {
        return franchiseRepository.findById(franchiseId)
            .switchIfEmpty(Mono.error(new EntityNotFound("franquicia", franchiseId)))
            .then(Mono.fromCallable(() -> Branch.builder()
                .name(nombre)
                .franchiseId(franchiseId)
                .build()))
            .flatMap(branchRepository::save);
    }
}
