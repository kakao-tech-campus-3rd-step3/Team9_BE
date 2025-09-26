package com.pado.domain.chat.dto.response;

import com.pado.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        Long messageId,
        Long senderId,
        String senderName,
        String content,
        LocalDateTime createdAt
) {
    
    public static ChatMessageResponseDto from(ChatMessage chatMessage) {
        return new ChatMessageResponseDto(
                chatMessage.getId(),
                chatMessage.getSender().getUser().getId(),
                chatMessage.getSender().getUser().getNickname(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }
}
