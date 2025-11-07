package com.pado.domain.chat.dto.response;

import java.util.List;

public record ChatMessageListResponseDto(
        Long userId,
        List<ChatMessageResponseDto> messages,
        boolean hasNext,
        Long nextCursor
) {
    
    public static ChatMessageListResponseDto of(Long userId, List<ChatMessageResponseDto> messages, boolean hasNext, Long nextCursor) {
        return new ChatMessageListResponseDto(userId, messages, hasNext, nextCursor);
    }
}
