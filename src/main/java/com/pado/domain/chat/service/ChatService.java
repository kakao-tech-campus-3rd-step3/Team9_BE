package com.pado.domain.chat.service;

import com.pado.domain.chat.dto.request.ChatMessageRequestDto;
import com.pado.domain.chat.dto.request.ReactionRequestDto;
import com.pado.domain.chat.dto.response.ChatMessageListResponseDto;
import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.dto.response.UnreadCountResponseDto;
import com.pado.domain.user.entity.User;

public interface ChatService {

    void sendMessage(Long studyId, ChatMessageRequestDto requestDto, User currentUser);

    ChatMessageListResponseDto getChatMessages(Long studyId, Long cursor, int size, User currentUser);

    void createChatReaction (Long studyId, Long chatMessageId, ReactionRequestDto request, User user);

    void updateChatReaction (Long studyId, Long chatMessageId, ReactionRequestDto request, User user);

    void deleteChatReaction (Long studyId, Long chatMessageId, User user);

    void deleteChatMessage(Long studyId, Long chatMessageId, User user);

    void openChatModal(Long studyId, User currentUser);

    void closeChatModal(Long studyId, User currentUser);

    void refreshModalState(Long studyId, User currentUser);

    UnreadCountResponseDto getUnreadMessages(Long studyId, User user);
}
