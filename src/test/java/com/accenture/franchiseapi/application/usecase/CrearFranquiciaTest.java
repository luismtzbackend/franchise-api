package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Franchise;
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
class CrearFranquiciaTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    private CrearFranquicia crearFranquicia;

    @BeforeEach
    void setUp() {
        crearFranquicia = new CrearFranquicia(franchiseRepository);
    }

    @Test
    void debeCrearFranquiciaExitosamente() {
        Franchise franquiciaEsperada = Franchise.builder()
                .id("f-1")
                .name("Franquicia Test")
                .build();

        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.just(franquiciaEsperada));

        StepVerifier.create(crearFranquicia.ejecutar("Franquicia Test"))
                .expectNext(franquiciaEsperada)
                .verifyComplete();

        verify(franchiseRepository).save(any(Franchise.class));
    }

    @Test
    void debeValidarNombreNoVacio() {
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("El nombre de la franquicia es obligatorio")));

        StepVerifier.create(crearFranquicia.ejecutar(""))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
