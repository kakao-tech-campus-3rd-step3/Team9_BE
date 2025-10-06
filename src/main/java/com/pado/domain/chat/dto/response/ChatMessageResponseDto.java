package com.pado.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.MessageType;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        Long messageId,
        MessageType messageType,
        Long senderId,
        String senderName,
        String content,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String link,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long likeCount,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long dislikeCount,
        Long unreadMemberCount,
        LocalDateTime createdAt
) {
    
    public static ChatMessageResponseDto from(
            ChatMessage chatMessage,
            Long likeCount,
            Long dislikeCount,
            Long unreadMemberCount) {

        Long senderId = chatMessage.getSender() != null ? chatMessage.getSender().getUser().getId() : null;
        String nickname = chatMessage.getSender() != null ? chatMessage.getSender().getUser().getNickname() : "서버";

        return new ChatMessageResponseDto(
                chatMessage.getId(),
                chatMessage.getType(),
                senderId,
                nickname,
                chatMessage.getContent(),
                chatMessage.getLink(),
                likeCount,
                dislikeCount,
                unreadMemberCount,
                chatMessage.getCreatedAt()
        );
    }
}
