package com.pado.domain.auth.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {
    private final StringRedisTemplate redis;
    private static final String KEY_PREFIX = "auth:refresh:";

    @Override
    public void saveToken(Long userId, String refreshToken, Duration ttl) {
        redis.opsForValue().set(KEY_PREFIX + userId, refreshToken, ttl);
    }

    @Override
    public Optional<String> getToken(Long userId) {
        return Optional.ofNullable(redis.opsForValue().get(KEY_PREFIX + userId));
    }

    @Override
    public void deleteToken(Long userId) {
        redis.delete(KEY_PREFIX + userId);
    }
}
