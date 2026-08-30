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
class ActualizarStockProductoTest {

    @Mock
    private ProductRepository productRepository;

    private ActualizarStockProducto actualizarStockProducto;

    @BeforeEach
    void setUp() {
        actualizarStockProducto = new ActualizarStockProducto(productRepository);
    }

    @Test
    void debeActualizarStockProductoExitosamente() {
        when(productRepository.updateStock("p-1", 250))
                .thenReturn(Mono.empty());

        StepVerifier.create(actualizarStockProducto.ejecutar("p-1", 250))
                .verifyComplete();

        verify(productRepository).updateStock("p-1", 250);
    }

    @Test
    void debeRechazarStockNegativo() {
        StepVerifier.create(actualizarStockProducto.ejecutar("p-1", -10))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void debePermitirStockCero() {
        when(productRepository.updateStock("p-1", 0))
                .thenReturn(Mono.empty());

        StepVerifier.create(actualizarStockProducto.ejecutar("p-1", 0))
                .verifyComplete();

        verify(productRepository).updateStock("p-1", 0);
    }
}
