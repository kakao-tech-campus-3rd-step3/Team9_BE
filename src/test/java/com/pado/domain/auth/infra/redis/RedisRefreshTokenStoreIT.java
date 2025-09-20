package com.pado.domain.auth.infra.redis;

import com.pado.support.RedisContainerTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisRefreshTokenStoreIT extends RedisContainerTestConfig {

    @Autowired
    private RefreshTokenStore store;

    @Test
    void 저장_조회_삭제_테스트() {
        Long userId = 100L;
        String rt = "dummy-refresh-token";

        store.saveToken(userId, rt, Duration.ofSeconds(10));
        assertThat(store.getToken(userId)).contains(rt);

        store.deleteToken(userId);
        assertThat(store.getToken(userId)).isEmpty();
    }
}
