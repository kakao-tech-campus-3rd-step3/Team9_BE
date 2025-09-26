package com.pado.domain.chat.controller;

import com.pado.domain.chat.dto.request.ChatMessageRequestDto;
import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.service.ChatService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.exception.common.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/studies/{studyId}/chats")
    public void sendMessage(
            @DestinationVariable Long studyId,
            @Valid ChatMessageRequestDto requestDto,
            @CurrentUser User currentUser) {

        ChatMessageResponseDto responseDto = chatService.sendMessage(studyId, requestDto, currentUser);
        messagingTemplate.convertAndSend( "/topic/studies/" + studyId + "/chats", responseDto);
    }
}
