package com.accenture.franchiseapi.infrastructure.drivenadapters.cacheredis;

import com.accenture.franchiseapi.domain.port.TopStockCache;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class TopStockRedisCache implements TopStockCache {

    private final ReactiveRedisTemplate<String, String> template;
    private static final String KEY_PREFIX = "top-stock:";
    private static final Duration TTL = Duration.ofHours(1);

    public TopStockRedisCache(ReactiveRedisTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    public Mono<List<String>> get(String franchiseId) {
        String key = KEY_PREFIX + franchiseId;
        return template.opsForList()
            .range(key, 0, -1)
            .collectList()
            .filter(list -> !list.isEmpty());
    }

    @Override
    public Mono<Void> put(String franchiseId, List<String> productIds) {
        String key = KEY_PREFIX + franchiseId;
        return template.delete(key)
            .then(template.opsForList()
                .rightPushAll(key, productIds))
            .then(template.expire(key, TTL))
            .then();
    }

    @Override
    public Mono<Void> evict(String franchiseId) {
        String key = KEY_PREFIX + franchiseId;
        return template.delete(key)
            .then();
    }
}
