package com.pado.domain.chat.dto.response;

import java.util.List;

public record ChatMessageListResponseDto(
        List<ChatMessageResponseDto> messages,
        boolean hasNext,
        Long nextCursor
) {
    
    public static ChatMessageListResponseDto of(List<ChatMessageResponseDto> messages, boolean hasNext, Long nextCursor) {
        return new ChatMessageListResponseDto(messages, hasNext, nextCursor);
    }
}
