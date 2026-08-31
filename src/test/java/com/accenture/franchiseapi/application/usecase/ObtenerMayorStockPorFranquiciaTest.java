package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.domain.port.ProductRepository;
import com.accenture.franchiseapi.domain.port.TopStockCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerMayorStockPorFranquiciaTest {

    @Mock
    private TopStockCache topStockCache;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BranchRepository branchRepository;

    private ObtenerMayorStockPorFranquicia obtenerMayorStockPorFranquicia;

    @BeforeEach
    void setUp() {
        obtenerMayorStockPorFranquicia = new ObtenerMayorStockPorFranquicia(
                topStockCache, productRepository, branchRepository);
    }

    @Test
    void debeObtenerProductosCacheados() {
        Product producto1 = Product.builder().id("p-1").branchId("b-1").name("Café").stock(100).build();
        Product producto2 = Product.builder().id("p-2").branchId("b-2").name("Té").stock(150).build();

        when(topStockCache.get("f-1"))
                .thenReturn(Mono.just(Arrays.asList("p-1", "p-2")));
        when(productRepository.findByIdIn(Arrays.asList("p-1", "p-2")))
                .thenReturn(Flux.just(producto1, producto2));

        StepVerifier.create(obtenerMayorStockPorFranquicia.ejecutar("f-1"))
                .expectNext(producto1, producto2)
                .verifyComplete();
    }

    @Test
    void debeConsultarBaseDatosYCachearSiNoHayCache() {
        Branch sucursal1 = Branch.builder().id("b-1").franchiseId("f-1").name("Sucursal 1").build();
        Branch sucursal2 = Branch.builder().id("b-2").franchiseId("f-1").name("Sucursal 2").build();
        Product producto1 = Product.builder().id("p-1").branchId("b-1").name("Café").stock(100).build();
        Product producto2 = Product.builder().id("p-2").branchId("b-2").name("Té").stock(150).build();

        when(topStockCache.get("f-1"))
                .thenReturn(Mono.empty());
        when(branchRepository.findByFranchiseId("f-1"))
                .thenReturn(Flux.just(sucursal1, sucursal2));
        when(productRepository.findByBranchIdIn(Arrays.asList("b-1", "b-2")))
                .thenReturn(Flux.just(producto1, producto2));
        when(topStockCache.put(anyString(), anyList()))
                .thenReturn(Mono.empty());

        StepVerifier.create(obtenerMayorStockPorFranquicia.ejecutar("f-1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void debeRechazarSiFranquiciaNoTieneProductos() {
        when(topStockCache.get("f-1"))
                .thenReturn(Mono.empty());
        when(branchRepository.findByFranchiseId("f-1"))
                .thenReturn(Flux.just(Branch.builder().id("b-1").franchiseId("f-1").name("Sucursal").build()));
        when(productRepository.findByBranchIdIn(Arrays.asList("b-1")))
                .thenReturn(Flux.empty());

        StepVerifier.create(obtenerMayorStockPorFranquicia.ejecutar("f-1"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
