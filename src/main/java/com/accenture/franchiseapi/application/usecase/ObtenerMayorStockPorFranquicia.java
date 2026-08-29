package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.domain.port.ProductRepository;
import com.accenture.franchiseapi.domain.port.TopStockCache;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObtenerMayorStockPorFranquicia {

    private final TopStockCache topStockCache;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public ObtenerMayorStockPorFranquicia(TopStockCache topStockCache,
                                          ProductRepository productRepository,
                                          BranchRepository branchRepository) {
        this.topStockCache = topStockCache;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    public Flux<Product> ejecutar(String franchiseId) {
        return topStockCache.get(franchiseId)
            .flatMapMany(productIds -> {
                if (productIds == null || productIds.isEmpty()) {
                    return Flux.empty();
                }
                return productRepository.findByBranchIdIn(productIds);
            })
            .switchIfEmpty(consultarProductosYCachear(franchiseId));
    }

    private Flux<Product> consultarProductosYCachear(String franchiseId) {
        return branchRepository.findByFranchiseId(franchiseId)
            .map(branch -> branch.getId())
            .collectList()
            .flatMapMany(branchIds ->
                productRepository.findByBranchIdIn(branchIds)
                    .collectList()
                    .flatMapMany(productos -> {
                        if (productos.isEmpty()) {
                            return Flux.error(new IllegalArgumentException("No hay productos en esta franquicia"));
                        }
                        Map<String, List<Product>> productosPorSucursal = productos.stream()
                            .collect(Collectors.groupingBy(Product::getBranchId));

                        List<Product> ganadores = productosPorSucursal.values().stream()
                            .map(this::obtenerMayorStock)
                            .collect(Collectors.toList());

                        List<String> ganadorIds = ganadores.stream()
                            .map(Product::getId)
                            .collect(Collectors.toList());

                        return topStockCache.put(franchiseId, ganadorIds)
                            .then(Mono.just(ganadores))
                            .flatMapMany(Flux::fromIterable);
                    })
            );
    }

    private Product obtenerMayorStock(List<Product> productos) {
        return productos.stream()
            .reduce((p1, p2) -> p1.hasMoreStockThan(p2) ? p1 : p2)
            .orElseThrow(() -> new IllegalArgumentException("No hay productos disponibles"));
    }
}
