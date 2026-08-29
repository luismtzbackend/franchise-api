package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.adapter;

import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.port.ProductRepository;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.mapper.ProductDocumentMapper;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.repository.ProductReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ProductMongoRepository implements ProductRepository {

    private final ProductReactiveMongoRepository reactiveRepository;
    private final ProductDocumentMapper mapper;

    public ProductMongoRepository(ProductReactiveMongoRepository reactiveRepository,
                                  ProductDocumentMapper mapper) {
        this.reactiveRepository = reactiveRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> save(Product product) {
        return Mono.just(mapper.toDocument(product))
            .flatMap(reactiveRepository::save)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Product> findById(String id) {
        return reactiveRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return reactiveRepository.deleteById(id);
    }

    @Override
    public Flux<Product> findByBranchId(String branchId) {
        return reactiveRepository.findByBranchId(branchId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Product> findTopStockByBranchId(String branchId) {
        return reactiveRepository.findTopByBranchIdOrderByStockDesc(branchId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Product> updateStock(String id, int newStock) {
        return reactiveRepository.findById(id)
            .flatMap(document -> {
                document.setStock(newStock);
                return reactiveRepository.save(document);
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Product> updateName(String id, String name) {
        return reactiveRepository.findById(id)
            .flatMap(document -> {
                document.setName(name);
                return reactiveRepository.save(document);
            })
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Product> findByBranchIdIn(List<String> branchIds) {
        return reactiveRepository.findByIdIn(branchIds)
            .map(mapper::toDomain);
    }
}
