package com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.adapter;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.port.BranchRepository;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.mapper.BranchDocumentMapper;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.repository.BranchReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class BranchMongoRepository implements BranchRepository {

    private final BranchReactiveMongoRepository reactiveRepository;
    private final BranchDocumentMapper mapper;

    public BranchMongoRepository(BranchReactiveMongoRepository reactiveRepository,
                                 BranchDocumentMapper mapper) {
        this.reactiveRepository = reactiveRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Branch> save(Branch branch) {
        return Mono.just(mapper.toDocument(branch))
            .flatMap(reactiveRepository::save)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Branch> findById(String id) {
        return reactiveRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Branch> findByFranchiseId(String franchiseId) {
        return reactiveRepository.findByFranchiseId(franchiseId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Branch> updateName(String id, String name) {
        return reactiveRepository.findById(id)
            .flatMap(document -> {
                document.setName(name);
                return reactiveRepository.save(document);
            })
            .map(mapper::toDomain);
    }
}
