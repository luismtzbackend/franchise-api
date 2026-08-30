package com.accenture.franchiseapi.infrastructure;

import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.adapter.FranchiseMongoRepository;
import com.accenture.franchiseapi.infrastructure.drivenadapters.persistencemongo.repository.FranchiseReactiveMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest
class FranchiseMongoRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private FranchiseMongoRepository franchiseMongoRepository;

    @Autowired
    private FranchiseReactiveMongoRepository reactiveRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        reactiveRepository.deleteAll().block();
    }

    @Test
    void debeGuardarFranquiciaEnMongo() {
        Franchise franquicia = Franchise.builder()
                .name("Franquicia Integración")
                .build();

        StepVerifier.create(franchiseMongoRepository.save(franquicia))
                .expectNextMatches(f -> f.getName().equals("Franquicia Integración"))
                .verifyComplete();
    }

    @Test
    void debeRecuperarFranquiciaGuardada() {
        Franchise franquicia = Franchise.builder()
                .name("Franquicia Test")
                .build();

        StepVerifier.create(
                franchiseMongoRepository.save(franquicia)
                        .flatMap(f -> franchiseMongoRepository.findById(f.getId()))
        )
                .expectNextMatches(f -> f.getName().equals("Franquicia Test"))
                .verifyComplete();
    }

    @Test
    void debeActualizarNombreFranquicia() {
        Franchise franquicia = Franchise.builder()
                .name("Nombre Original")
                .build();

        StepVerifier.create(
                franchiseMongoRepository.save(franquicia)
                        .flatMap(f -> franchiseMongoRepository.updateName(f.getId(), "Nombre Actualizado"))
                        .flatMap(f -> franchiseMongoRepository.findById(f.getId()))
        )
                .expectNextMatches(f -> f.getName().equals("Nombre Actualizado"))
                .verifyComplete();
    }
}
