package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActualizarNombreSucursalTest {

    @Mock
    private BranchRepository branchRepository;

    private ActualizarNombreSucursal actualizarNombreSucursal;

    @BeforeEach
    void setUp() {
        actualizarNombreSucursal = new ActualizarNombreSucursal(branchRepository);
    }

    @Test
    void debeActualizarNombreSucursalExitosamente() {
        when(branchRepository.updateName("b-1", "Nueva Sucursal"))
                .thenReturn(Mono.empty());

        StepVerifier.create(actualizarNombreSucursal.ejecutar("b-1", "Nueva Sucursal"))
                .verifyComplete();

        verify(branchRepository).updateName("b-1", "Nueva Sucursal");
    }

    @Test
    void debeHandlearErrorAlActualizarNombreSucursal() {
        when(branchRepository.updateName(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("Sucursal no encontrada")));

        StepVerifier.create(actualizarNombreSucursal.ejecutar("b-999", "Nombre"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
