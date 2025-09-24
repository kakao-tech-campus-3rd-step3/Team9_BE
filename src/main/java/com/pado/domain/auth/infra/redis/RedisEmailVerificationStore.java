package com.pado.domain.auth.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RedisEmailVerificationStore implements EmailVerificationStore {
    private final StringRedisTemplate redis;
    private static final String KEY_PREFIX = "email:verify:code:";

    @Override
    public void saveCode(String email, String code, Duration ttl) {
        redis.opsForValue().set(KEY_PREFIX + email, code, ttl);
    }

    @Override
    public Optional<String> getCode(String email) {
        return Optional.ofNullable(redis.opsForValue().get(KEY_PREFIX + email));
    }

    @Override
    public void deleteCode(String email) {
        redis.delete(KEY_PREFIX + email);
    }
}
