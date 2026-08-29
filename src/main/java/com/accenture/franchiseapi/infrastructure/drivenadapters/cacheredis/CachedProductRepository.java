package com.accenture.franchiseapi.infrastructure.drivenadapters.cacheredis;

import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.port.ProductRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class CachedProductRepository implements ProductRepository {

    private final ProductRepository delegate;
    private final ReactiveRedisTemplate<String, String> template;
    private final TopStockRedisCache topStockCache;
    private static final String BRANCH_CACHE_PREFIX = "branch:";
    private static final Duration BRANCH_TTL = Duration.ofMinutes(15);

    public CachedProductRepository(ProductRepository delegate,
                                   ReactiveRedisTemplate<String, String> template,
                                   TopStockRedisCache topStockCache) {
        this.delegate = delegate;
        this.template = template;
        this.topStockCache = topStockCache;
    }

    @Override
    public Mono<Product> save(Product product) {
        return delegate.save(product)
            .flatMap(saved -> invalidateBranchCache(saved.getBranchId())
                .then(Mono.just(saved)));
    }

    @Override
    public Mono<Product> findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return delegate.findById(id)
            .flatMap(product -> delegate.deleteById(id)
                .then(invalidateBranchCache(product.getBranchId())))
            .switchIfEmpty(delegate.deleteById(id));
    }

    @Override
    public Flux<Product> findByBranchId(String branchId) {
        String cacheKey = BRANCH_CACHE_PREFIX + branchId;
        return template.opsForValue()
            .get(cacheKey)
            .flatMapMany(v -> delegate.findByBranchId(branchId))
            .switchIfEmpty(delegate.findByBranchId(branchId)
                .collectList()
                .flatMapMany(products -> markBranchCached(branchId)
                    .thenMany(Flux.fromIterable(products))));
    }

    @Override
    public Mono<Product> findTopStockByBranchId(String branchId) {
        return delegate.findTopStockByBranchId(branchId);
    }

    @Override
    public Mono<Product> updateStock(String id, int newStock) {
        return delegate.findById(id)
            .flatMap(product -> delegate.updateStock(id, newStock)
                .flatMap(updated -> invalidateBranchCache(product.getBranchId())
                    .then(Mono.just(updated))));
    }

    @Override
    public Mono<Product> updateName(String id, String name) {
        return delegate.findById(id)
            .flatMap(product -> delegate.updateName(id, name)
                .flatMap(updated -> invalidateBranchCache(product.getBranchId())
                    .then(Mono.just(updated))));
    }

    @Override
    public Flux<Product> findByBranchIdIn(List<String> branchIds) {
        return delegate.findByBranchIdIn(branchIds);
    }

    private Mono<Void> invalidateBranchCache(String branchId) {
        String cacheKey = BRANCH_CACHE_PREFIX + branchId;
        return template.delete(cacheKey)
            .then(topStockCache.evict(branchId));
    }

    private Mono<Void> markBranchCached(String branchId) {
        String cacheKey = BRANCH_CACHE_PREFIX + branchId;
        return template.opsForValue()
            .set(cacheKey, "1", BRANCH_TTL)
            .then();
    }
}
