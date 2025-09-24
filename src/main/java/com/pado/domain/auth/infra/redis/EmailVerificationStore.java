package com.pado.domain.auth.infra.redis;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationStore {
    void saveCode(String email, String code, Duration ttl);
    Optional<String> getCode(String email);
    void deleteCode(String email);
}
