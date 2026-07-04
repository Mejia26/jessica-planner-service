package com.guido.agiletaskservice.infrastructure.cache;

import com.guido.agiletaskservice.application.port.InMemoryKeyValueStoreService;
import com.guido.agiletaskservice.config.CacheProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.cache", name = "provider", havingValue = "REDIS")
public class RedisInMemoryKeyValueStoreService implements InMemoryKeyValueStoreService {

    private final StringRedisTemplate redisTemplate;
    private final CacheProperties cacheProperties;

    @Override
    public void put(String key, String value, Duration ttl) {
        String resolvedKey = resolveKey(key);
        redisTemplate.opsForValue().set(resolvedKey, value, ttl);
        log.debug("Stored value in Redis cache: key={}, ttlSeconds={}", resolvedKey, ttl.toSeconds());
    }

    @Override
    public Optional<String> get(String key) {
        String resolvedKey = resolveKey(key);
        String value = redisTemplate.opsForValue().get(resolvedKey);
        log.debug("Read value from Redis cache: key={}, hit={}", resolvedKey, value != null);
        return Optional.ofNullable(value);
    }

    @Override
    public void evict(String key) {
        String resolvedKey = resolveKey(key);
        redisTemplate.delete(resolvedKey);
        log.debug("Evicted value from Redis cache: key={}", resolvedKey);
    }

    private String resolveKey(String key) {
        String prefix = cacheProperties.keyPrefix() == null ? "agile-task-service" : cacheProperties.keyPrefix();
        return prefix + ":" + key;
    }
}
