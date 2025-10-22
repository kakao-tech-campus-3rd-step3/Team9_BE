package com.pado.domain.chat.repository;

import com.pado.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findChatMessagesWithCursor(Long studyId, Long cursor, int size);
}
