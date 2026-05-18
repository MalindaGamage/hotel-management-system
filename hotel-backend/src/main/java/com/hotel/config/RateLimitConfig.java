package com.hotel.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Value("${app.rate-limit.auth-requests}")
    private int authRequests;

    @Value("${app.rate-limit.auth-window-seconds}")
    private int windowSeconds;

    // Per-IP bucket cache for auth endpoint rate limiting
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ipAddress) {
        return buckets.computeIfAbsent(ipAddress, this::newBucket);
    }

    private Bucket newBucket(String ignored) {
        Bandwidth limit = Bandwidth.classic(
            authRequests,
            Refill.intervally(authRequests, Duration.ofSeconds(windowSeconds))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
