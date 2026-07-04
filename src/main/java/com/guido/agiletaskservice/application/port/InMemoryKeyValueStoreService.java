package com.guido.agiletaskservice.application.port;

import java.time.Duration;
import java.util.Optional;

public interface InMemoryKeyValueStoreService {

    void put(String key, String value, Duration ttl);

    Optional<String> get(String key);

    void evict(String key);
}
