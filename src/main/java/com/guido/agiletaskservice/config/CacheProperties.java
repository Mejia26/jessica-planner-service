package com.guido.agiletaskservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(
        Provider provider,
        String keyPrefix,
        Long defaultTtlSeconds
) {
    public enum Provider {
        NONE,
        REDIS
    }
}
