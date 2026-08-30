package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.ProductRepository;
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
class ActualizarNombreProductoTest {

    @Mock
    private ProductRepository productRepository;

    private ActualizarNombreProducto actualizarNombreProducto;

    @BeforeEach
    void setUp() {
        actualizarNombreProducto = new ActualizarNombreProducto(productRepository);
    }

    @Test
    void debeActualizarNombreProductoExitosamente() {
        when(productRepository.updateName("p-1", "Nuevo Producto"))
                .thenReturn(Mono.empty());

        StepVerifier.create(actualizarNombreProducto.ejecutar("p-1", "Nuevo Producto"))
                .verifyComplete();

        verify(productRepository).updateName("p-1", "Nuevo Producto");
    }

    @Test
    void debeHandlearErrorAlActualizarNombreProducto() {
        when(productRepository.updateName(anyString(), anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("Producto no encontrado")));

        StepVerifier.create(actualizarNombreProducto.ejecutar("p-999", "Nombre"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
