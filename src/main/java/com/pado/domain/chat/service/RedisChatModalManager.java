package com.pado.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisChatModalManager {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String MODAL_KEY_PREFIX = "chat:modal:";
    private static final long MODAL_TTL = 300; // 5분 (초 단위)


    // 채팅 모달 열기
    // Redis에 "chat:modal:{studyId}:{userId}" = "true" 저장 (TTL 5분)
    public void openModal(Long studyId, Long userId) {
        String key = getModalKey(studyId, userId);
        redisTemplate.opsForValue().set(key, "true", MODAL_TTL, TimeUnit.SECONDS);
    }


    // 채팅 모달 닫기
    // Redis에서 키 삭제
    public void closeModal(Long studyId, Long userId) {
        String key = getModalKey(studyId, userId);
        redisTemplate.delete(key);
    }


    // 모달이 열려있는지 확인
    public boolean isModalOpen(Long studyId, Long userId) {
        String key = getModalKey(studyId, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 모달이 열려있는 상태면 주기적으로 TTL 갱신
    public void refreshModal(Long studyId, Long userId) {
        String key = getModalKey(studyId, userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, MODAL_TTL, TimeUnit.SECONDS);
        }
    }


    // 특정 스터디에서 모달이 열린 모든 사용자 ID 조회
    public Set<Long> getOpenModalUserIds(Long studyId) {
        String pattern = MODAL_KEY_PREFIX + studyId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys.isEmpty()) {
            return Set.of();
        }

        return keys.stream()
                .map(key -> {
                    String[] parts = key.split(":");
                    return Long.parseLong(parts[parts.length - 1]);
                })
                .collect(Collectors.toSet());
    }

    private String getModalKey(Long studyId, Long userId) {
        return MODAL_KEY_PREFIX + studyId + ":" + userId;
    }
}