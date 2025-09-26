package com.pado.domain.auth.infra.redis;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {
    void saveToken(Long userId, String token, Duration ttl);
    Optional<String> getToken(Long userId);
    void deleteToken(Long userId);
}