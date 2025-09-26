package com.pado.domain.chat.controller;

import com.pado.domain.chat.dto.response.ChatMessageListResponseDto;
import com.pado.domain.chat.service.ChatService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chat", description = "채팅 API")
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "채팅 기록 조회", description = "커서 페이지네이션을 통해 채팅 기록을 조회합니다. (20개씩 불러옴)")
    @GetMapping("/{studyId}/chats")
    public ResponseEntity<ChatMessageListResponseDto> getChatMessages(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "마지막으로 받은 메시지 ID (null이면 최신부터)") @RequestParam(required = false) Long cursor,
            @Parameter(description = "조회할 메시지 수") @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @CurrentUser User user) {

        ChatMessageListResponseDto response = chatService.getChatMessages(studyId, cursor, size, user);
        return ResponseEntity.ok(response);
    }

}
