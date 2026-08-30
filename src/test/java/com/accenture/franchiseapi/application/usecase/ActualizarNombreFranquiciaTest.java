package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.FranchiseRepository;
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
class ActualizarNombreFranquiciaTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    private ActualizarNombreFranquicia actualizarNombreFranquicia;

    @BeforeEach
    void setUp() {
        actualizarNombreFranquicia = new ActualizarNombreFranquicia(franchiseRepository);
    }

    @Test
    void debeActualizarNombreFranquiciaExitosamente() {
        when(franchiseRepository.updateName("f-1", "Nuevo Nombre"))
                .thenReturn(Mono.empty());

        StepVerifier.create(actualizarNombreFranquicia.ejecutar("f-1", "Nuevo Nombre"))
                .verifyComplete();

        verify(franchiseRepository).updateName("f-1", "Nuevo Nombre");
    }

    @Test
    void debeHandlearErrorAlActualizarNombreFranquicia() {
        when(franchiseRepository.updateName(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("Franquicia no encontrada")));

        StepVerifier.create(actualizarNombreFranquicia.ejecutar("f-999", "Nombre"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
