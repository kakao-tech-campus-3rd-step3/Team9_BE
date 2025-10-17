package com.pado.domain.chat.controller;

import com.pado.domain.chat.dto.request.ChatMessageRequestDto;
import com.pado.domain.chat.service.ChatService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/studies/{studyId}/chats")
    public void sendMessage(
            @DestinationVariable Long studyId,
            @Valid ChatMessageRequestDto requestDto,
            @CurrentUser User currentUser) {

        chatService.sendMessage(studyId, requestDto, currentUser);
    }

    @MessageMapping("/studies/{studyId}/modal/open")
    public void openChatModal(
            @DestinationVariable Long studyId,
            @CurrentUser User currentUser) {

        chatService.openChatModal(studyId, currentUser);
    }

    @MessageMapping("/studies/{studyId}/modal/close")
    public void closeChatModal(
            @DestinationVariable Long studyId,
            @CurrentUser User currentUser) {

        chatService.closeChatModal(studyId, currentUser);
    }

    @MessageMapping("/studies/{studyId}/modal/heartbeat")
    public void modalHeartbeat(
            @DestinationVariable Long studyId,
            @CurrentUser User currentUser) {

        chatService.refreshModalState(studyId, currentUser);
    }
}