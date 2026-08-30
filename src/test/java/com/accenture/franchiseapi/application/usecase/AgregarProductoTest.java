package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.domain.port.ProductRepository;
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
class AgregarProductoTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchRepository branchRepository;

    private AgregarProducto agregarProducto;

    @BeforeEach
    void setUp() {
        agregarProducto = new AgregarProducto(productRepository, branchRepository);
    }

    @Test
    void debeAgregarProductoASucursalExistente() {
        Branch sucursal = Branch.builder().id("b-1").franchiseId("f-1").name("Sucursal Test").build();
        Product producto = Product.builder().id("p-1").branchId("b-1").name("Café").stock(100).build();

        when(branchRepository.findById("b-1"))
                .thenReturn(Mono.just(sucursal));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(producto));

        StepVerifier.create(agregarProducto.ejecutar("b-1", "Café", 100))
                .expectNext(producto)
                .verifyComplete();

        verify(branchRepository).findById("b-1");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void debeRechazarProductoParaSucursalInexistente() {
        when(branchRepository.findById("b-999"))
                .thenReturn(Mono.empty());

        StepVerifier.create(agregarProducto.ejecutar("b-999", "Café", 50))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
