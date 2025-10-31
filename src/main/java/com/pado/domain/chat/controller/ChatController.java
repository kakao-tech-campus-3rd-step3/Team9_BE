package com.pado.domain.chat.controller;

import com.pado.domain.chat.dto.request.ReactionRequestDto;
import com.pado.domain.chat.dto.response.ChatMessageListResponseDto;
import com.pado.domain.chat.dto.response.UnreadCountResponseDto;
import com.pado.domain.chat.service.ChatService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "좋아요/싫어요 생성", description = "특정 메시지에 좋아요 또는 싫어요를 추가합니다.")
    @PostMapping("/{studyId}/chats/{chatId}/reactions")
    public ResponseEntity<Void> createReaction (
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "채팅 메시지 ID") @PathVariable Long chatId,
            @RequestBody ReactionRequestDto request,
            @Parameter(hidden = true) @CurrentUser User user) {

        chatService.createChatReaction(studyId, chatId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "좋아요/싫어요 수정", description = "특정 메시지의 리액션을 변경합니다.")
    @PutMapping("/{studyId}/chats/{chatId}/reactions")
    public ResponseEntity<Void> updateReaction (
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "채팅 메시지 ID") @PathVariable Long chatId,
            @RequestBody ReactionRequestDto request,
            @Parameter(hidden = true) @CurrentUser User user) {

        chatService.updateChatReaction(studyId, chatId, request, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "좋아요/싫어요 삭제", description = "특정 메시지의 리액션을 삭제합니다.")
    @DeleteMapping("/{studyId}/chats/{chatId}/reactions")
    public ResponseEntity<Void> deleteReaction (
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "채팅 메시지 ID") @PathVariable Long chatId,
            @Parameter(hidden = true) @CurrentUser User user) {

        chatService.deleteChatReaction(studyId, chatId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "채팅 메시지 삭제", description = "본인이 작성한 채팅 메시지를 삭제합니다.")
    @DeleteMapping("/{studyId}/chats/{chatId}")
    public ResponseEntity<Void> deleteChatMessage (
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(description = "채팅 메시지 ID") @PathVariable Long chatId,
            @Parameter(hidden = true) @CurrentUser User user) {

        chatService.deleteChatMessage(studyId, chatId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "안읽은 채팅 메세지 수 조회", description = "안읽은 메세지 수를 조회합니다.")
    @GetMapping("/{studyId}/chats/unread")
    public ResponseEntity<UnreadCountResponseDto> getChatMessages(
            @Parameter(description = "스터디 ID") @PathVariable Long studyId,
            @Parameter(hidden = true) @CurrentUser User user) {

        UnreadCountResponseDto response = chatService.getUnreadMessages(studyId, user);
        return ResponseEntity.ok(response);
    }

}
