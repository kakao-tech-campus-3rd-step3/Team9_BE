package com.pado.domain.auth.infra.redis;

import com.pado.support.RedisContainerTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisTtlIT extends RedisContainerTestConfig {

    @Autowired
    private EmailVerificationStore store;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    void ttl이_만료_시_삭제() throws InterruptedException {
        String email = "ttl@example.com";
        String key = "email:verify:code:" + email;

        store.saveCode(email, "654321", Duration.ofSeconds(2));
        Long ttl1 = redis.getExpire(key);
        assertThat(ttl1).isPositive();
        Thread.sleep(2500);
        assertThat(store.getCode(email)).isEmpty();
    }
}
