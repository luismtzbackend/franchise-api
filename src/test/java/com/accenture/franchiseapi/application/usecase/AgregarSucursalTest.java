package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.domain.port.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgregarSucursalTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private FranchiseRepository franchiseRepository;

    private AgregarSucursal agregarSucursal;

    @BeforeEach
    void setUp() {
        agregarSucursal = new AgregarSucursal(branchRepository, franchiseRepository);
    }

    @Test
    void debeAgregarSucursalAFranquiciaExistente() {
        Franchise franquicia = Franchise.builder().id("f-1").name("Franquicia Test").build();
        Branch sucursal = Branch.builder().id("b-1").franchiseId("f-1").name("Sucursal Test").build();

        when(franchiseRepository.findById("f-1"))
                .thenReturn(Mono.just(franquicia));
        when(branchRepository.save(any(Branch.class)))
                .thenReturn(Mono.just(sucursal));

        StepVerifier.create(agregarSucursal.ejecutar("f-1", "Sucursal Test"))
                .expectNext(sucursal)
                .verifyComplete();

        verify(franchiseRepository).findById("f-1");
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void debeRechazarSucursalParaFranquiciaInexistente() {
        when(franchiseRepository.findById("f-999"))
                .thenReturn(Mono.empty());

        StepVerifier.create(agregarSucursal.ejecutar("f-999", "Sucursal Test"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
