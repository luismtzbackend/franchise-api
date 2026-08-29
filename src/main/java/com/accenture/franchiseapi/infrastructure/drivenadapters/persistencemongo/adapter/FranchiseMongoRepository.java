package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.adapter;

import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.port.FranchiseRepository;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.mapper.FranchiseDocumentMapper;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.repository.FranchiseReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FranchiseMongoRepository implements FranchiseRepository {

    private final FranchiseReactiveMongoRepository reactiveRepository;
    private final FranchiseDocumentMapper mapper;

    public FranchiseMongoRepository(FranchiseReactiveMongoRepository reactiveRepository,
                                    FranchiseDocumentMapper mapper) {
        this.reactiveRepository = reactiveRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return Mono.just(mapper.toDocument(franchise))
            .flatMap(reactiveRepository::save)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return reactiveRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Franchise> updateName(String id, String name) {
        return reactiveRepository.findById(id)
            .flatMap(document -> {
                document.setName(name);
                return reactiveRepository.save(document);
            })
            .map(mapper::toDomain);
    }
}
