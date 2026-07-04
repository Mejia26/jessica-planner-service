package com.guido.agiletaskservice.infrastructure.cache;

import com.guido.agiletaskservice.application.port.InMemoryKeyValueStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnMissingBean(InMemoryKeyValueStoreService.class)
public class NoOpInMemoryKeyValueStoreService implements InMemoryKeyValueStoreService {

    @Override
    public void put(String key, String value, Duration ttl) {
        log.debug("Skipping cache put because no in-memory key-value store is enabled: key={}", key);
    }

    @Override
    public Optional<String> get(String key) {
        log.debug("Skipping cache get because no in-memory key-value store is enabled: key={}", key);
        return Optional.empty();
    }

    @Override
    public void evict(String key) {
        log.debug("Skipping cache eviction because no in-memory key-value store is enabled: key={}", key);
    }
}
