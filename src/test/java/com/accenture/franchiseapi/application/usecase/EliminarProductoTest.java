package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EliminarProductoTest {

    @Mock
    private ProductRepository productRepository;

    private EliminarProducto eliminarProducto;

    @BeforeEach
    void setUp() {
        eliminarProducto = new EliminarProducto(productRepository);
    }

    @Test
    void debeEliminarProductoExitosamente() {
        when(productRepository.deleteById("p-1"))
                .thenReturn(Mono.empty());

        StepVerifier.create(eliminarProducto.ejecutar("p-1"))
                .verifyComplete();

        verify(productRepository).deleteById("p-1");
    }

    @Test
    void debeHandlearErrorAlEliminarProducto() {
        when(productRepository.deleteById("p-999"))
                .thenReturn(Mono.error(new IllegalArgumentException("Producto no encontrado")));

        StepVerifier.create(eliminarProducto.ejecutar("p-999"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
