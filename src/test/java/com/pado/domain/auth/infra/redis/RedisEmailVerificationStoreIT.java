package com.pado.domain.auth.infra.redis;

import com.pado.support.RedisContainerTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisEmailVerificationStoreIT extends RedisContainerTestConfig {

    @Autowired
    private EmailVerificationStore store;

    @Test
    public void 저장_조회_삭제_테스트() {
        String email = "test@example.com";
        String code = "123456";

        store.saveCode(email, code, Duration.ofSeconds(5));

        // 조회
        String saved = store.getCode(email).orElse(null);
        assertThat(saved).isEqualTo(code);

        // 삭제
        store.deleteCode(email);
        assertThat(store.getCode(email)).isEmpty();
    }
}
