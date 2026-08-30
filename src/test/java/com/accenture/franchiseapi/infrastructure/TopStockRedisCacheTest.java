package com.accenture.franchiseapi.infrastructure;

import com.accenture.franchiseapi.infrastructure.drivenadapters.cacheredis.TopStockRedisCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.Arrays;

@Testcontainers
@SpringBootTest
class TopStockRedisCacheTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private TopStockRedisCache topStockRedisCache;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getFirstMappedPort());
    }

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();
    }

    @Test
    void debeGuardarProductosEnCache() {
        StepVerifier.create(
                topStockRedisCache.put("f-1", Arrays.asList("p-1", "p-2"))
        )
                .verifyComplete();
    }

    @Test
    void debeRecuperarProductosCacheados() {
        StepVerifier.create(
                topStockRedisCache.put("f-1", Arrays.asList("p-1", "p-2"))
                        .then(topStockRedisCache.get("f-1"))
        )
                .expectNext(Arrays.asList("p-1", "p-2"))
                .verifyComplete();
    }

    @Test
    void debeEvictarCacheDeProductos() {
        StepVerifier.create(
                topStockRedisCache.put("f-1", Arrays.asList("p-1", "p-2"))
                        .then(topStockRedisCache.evict("f-1"))
                        .then(topStockRedisCache.get("f-1"))
        )
                .verifyComplete();
    }

    @Test
    void debeRetornarEmptyParaCacheNoExistente() {
        StepVerifier.create(topStockRedisCache.get("f-999"))
                .verifyComplete();
    }
}
