package com.pado.domain.chat.service;

import com.pado.domain.chat.dto.request.ChatMessageRequestDto;
import com.pado.domain.chat.dto.request.ReactionRequestDto;
import com.pado.domain.chat.dto.response.ChatMessageListResponseDto;
import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.dto.response.ChatReactionCountDto;
import com.pado.domain.chat.dto.response.LastReadMessageResponseDto;
import com.pado.domain.chat.dto.response.UnreadCountResponseDto;
import com.pado.domain.chat.dto.response.UpdatedChatRoomResponseDto;
import com.pado.domain.chat.entity.*;
import com.pado.domain.chat.repository.ChatReactionRepository;
import com.pado.domain.chat.repository.LastReadMessageRepository;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final LastReadMessageRepository lastReadRepository;
    private final RedisChatModalManager modalManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatReactionRepository chatReactionRepository;

    @Override
    @Transactional
    public void sendMessage(Long studyId, ChatMessageRequestDto requestDto, User currentUser) {
        validateMessageContent(requestDto.content());

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        ChatMessage chatMessage = ChatMessage.builder()
                .study(study)
                .type(MessageType.CHAT)
                .sender(studyMember)
                .content(requestDto.content())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 현재 채팅방에 접속하고 있는 멤버 대상으로만 마지막으로 읽은 메세지 id 업데이트
        Set<Long> onlineUserIds = modalManager.getOpenSocketUserIds(studyId);
        if (!onlineUserIds.isEmpty()) {
            // 1. 채팅방에 접속한 유저의 StudyMember 목록을 한 번에 조회
            List<StudyMember> onlineMembers = studyMemberRepository.findByStudyIdAndUserIdIn(studyId, onlineUserIds);

            // 2. StudyMember 목록으로 LastReadMessage 목록을 한 번에 조회
            List<LastReadMessage> lastReadMessages = lastReadRepository.findByStudyMemberIn(onlineMembers);

            // 3. DB 업데이트
            lastReadMessages.forEach(lrm -> lrm.updateLastReadMessageId(savedMessage.getId()));
        }

        // 스터디 전체 멤버 - 채팅방 접속 중인 멤버 방식으로 계산
        Long unreadMemberCount = lastReadRepository.countUnreadMembers(studyId, savedMessage.getId());
        ChatReactionCountDto reactionCount = CountMessageReaction(savedMessage);

        // 채팅 전송
        ChatMessageResponseDto responseDto = ChatMessageResponseDto.from(savedMessage, reactionCount.likeCount(), reactionCount.dislikeCount(), unreadMemberCount);
        messagingTemplate.convertAndSend("/topic/studies/" + studyId + "/chats", responseDto);

        // 현재 채팅방에 접속하고 있지 않은 사용자들에게 안읽은 메시지 수 알림을 비동기 처리
        sendUnreadCountToClosedModalUsersAsync(studyId);
    }

    @Override
    public ChatMessageListResponseDto getChatMessages(Long studyId, Long cursor, int size, User currentUser) {
        findAndValidateStudyMember(studyId, currentUser);

        List<ChatMessage> chatMessages = chatMessageRepository.findChatMessagesWithCursor(studyId, cursor, size);

        boolean hasNext = chatMessages.size() > size;
        if (hasNext) {
            chatMessages = chatMessages.subList(0, size);
        }

        List<Long> messageIds = chatMessages.stream()
                .map(ChatMessage::getId)
                .toList();

        Map<Long, Long> unreadCounts = lastReadRepository.getUnreadCountsForMessages(studyId, messageIds);

        // CHAT 타입 메시지의 ID만 추출 (리액션은 CHAT 타입에만 존재)
        List<Long> chatMessageIds = chatMessages.stream()
                .filter(msg -> msg.getType() == MessageType.CHAT)
                .map(ChatMessage::getId)
                .toList();

        // CHAT 타입 메시지에 대해서만 리액션 카운트 조회
        Map<Long, ChatReactionCountDto> reactionMap = chatMessageIds.isEmpty() 
                ? Map.of() 
                : chatReactionRepository.findReactionCountsByMessageIdIn(chatMessageIds).stream()
                    .collect(Collectors.toMap(
                            ChatReactionCountDto::messageId,
                            dto -> dto
                    ));

        List<ChatMessageResponseDto> messageDtos = chatMessages.stream()
                .map(msg -> {
                    // CHAT 타입일 때만 리액션 카운트 포함, 아니면 null
                    if (msg.getType() == MessageType.CHAT) {
                        ChatReactionCountDto reactionCount = reactionMap.get(msg.getId());
                        return ChatMessageResponseDto.from(
                                msg,
                                reactionCount != null ? reactionCount.likeCount() : 0L,
                                reactionCount != null ? reactionCount.dislikeCount() : 0L,
                                unreadCounts.getOrDefault(msg.getId(), 0L)
                        );
                    } else {
                        // NOTICE, SCHEDULE 타입은 리액션 카운트 null
                        return ChatMessageResponseDto.from(
                                msg,
                                null,
                                null,
                                unreadCounts.getOrDefault(msg.getId(), 0L)
                        );
                    }
                })
                .toList();

        Long nextCursor;

        if (hasNext && !messageDtos.isEmpty()) {
            nextCursor = messageDtos.get(messageDtos.size() - 1).messageId();
        }
        else {
            nextCursor = null;
        }

        return ChatMessageListResponseDto.of(messageDtos, hasNext, nextCursor);
    }

    @Transactional
    @Override
    public void createChatReaction(Long studyId, Long chatMessageId, ReactionRequestDto request, User user) {
        StudyMember member = findAndValidateStudyMember(studyId, user);
        ChatMessage message = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        if (message.getType() != MessageType.CHAT) {
            throw new BusinessException(ErrorCode.INVALID_REACTION_TARGET);
        }

        if (chatReactionRepository.existsByStudyMemberIdAndChatMessageId(member.getId(), message.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_REACTED);
        }

        ReactionType reactionType = ReactionType.fromString(request.reaction());

        ChatReaction chatReaction = ChatReaction.builder()
                .chatMessage(message)
                .studyMember(member)
                .reactionType(reactionType)
                .build();

        chatReactionRepository.save(chatReaction);

        ChatReactionCountDto reactionCount = CountMessageReaction(message);

        UpdatedChatRoomResponseDto responseDto = new UpdatedChatRoomResponseDto(
                UpdateType.IMOJI,
                chatMessageId,
                reactionCount.likeCount(),
                reactionCount.dislikeCount()
        );
        messagingTemplate.convertAndSend("/topic/studies/" + studyId + "/updates", responseDto);
    }

    @Transactional
    @Override
    public void updateChatReaction(Long studyId, Long chatMessageId, ReactionRequestDto request, User user) {
        StudyMember member = findAndValidateStudyMember(studyId, user);
        ChatMessage message = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
        ChatReaction chatReaction = chatReactionRepository.findByChatMessageAndStudyMember(message, member)
                .orElseThrow(() -> new BusinessException(ErrorCode.REACTION_NOT_FOUND));

        ReactionType newReactionType = ReactionType.fromString(request.reaction());

        chatReaction.changeReaction(newReactionType);

        ChatReactionCountDto reactionCount = CountMessageReaction(message);

        UpdatedChatRoomResponseDto responseDto = new UpdatedChatRoomResponseDto(
                UpdateType.IMOJI,
                chatMessageId,
                reactionCount.likeCount(),
                reactionCount.dislikeCount()
        );
        messagingTemplate.convertAndSend("/topic/studies/" + studyId + "/updates", responseDto);
    }

    @Transactional
    @Override
    public void deleteChatReaction(Long studyId, Long chatMessageId, User user) {
        StudyMember member = findAndValidateStudyMember(studyId, user);
        ChatMessage message = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
        ChatReaction chatReaction = chatReactionRepository.findByChatMessageAndStudyMember(message, member)
                .orElseThrow(() -> new BusinessException(ErrorCode.REACTION_NOT_FOUND));

        chatReactionRepository.delete(chatReaction);

        ChatReactionCountDto reactionCount = CountMessageReaction(message);

        UpdatedChatRoomResponseDto responseDto = new UpdatedChatRoomResponseDto(
                UpdateType.IMOJI,
                chatMessageId,
                reactionCount.likeCount(),
                reactionCount.dislikeCount()
        );
        messagingTemplate.convertAndSend("/topic/studies/" + studyId + "/updates", responseDto);
    }

    @Transactional
    @Override
    public void deleteChatMessage(Long studyId, Long chatMessageId, User user) {
        findAndValidateStudyMember(studyId, user);
        ChatMessage message = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));

        if (message.getType() != MessageType.CHAT) {
            throw new BusinessException(ErrorCode.INVALID_MESSAGE_TYPE_FOR_DELETION);
        }

        if (!message.getSender().getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_DELETE_MESSAGE);
        }

        chatReactionRepository.deleteAllByChatMessageId(message.getId());
        chatMessageRepository.delete(message);

        UpdatedChatRoomResponseDto responseDto = new UpdatedChatRoomResponseDto(
                UpdateType.DELETED,
                chatMessageId,
                null,
                null
        );
        messagingTemplate.convertAndSend("/topic/studies/" + studyId + "/updates", responseDto);
    }

    @Override
    @Transactional
    public void openChatModal(Long studyId, User currentUser) {

        modalManager.openSocket(studyId, currentUser.getId());

        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        // 현재 채팅방에서 가장 최신 메시지 아이디 추출
        Optional<ChatMessage> latestMessage = chatMessageRepository.findTopByStudyIdOrderByIdDesc(studyId);
        long latestChatMessageId = chatMessageRepository.findTopByStudyIdOrderByIdDesc(studyId)
                .map(ChatMessage::getId)
                .orElse(0L);

        // 모달을 킨 유저가 가장 마지막으로 읽었던 메세지 아이디 추출
        LastReadMessage lastReadMessage = lastReadRepository.findByStudyMember(studyMember)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LAST_READ_CHAT));
        long lastReadMessageId = lastReadMessage.getLastReadMessageId();

        lastReadMessage.updateLastReadMessageId(latestChatMessageId);

        // 모달을 킨 유저가 가장 마지막으로 읽었던 메세지 아이디 전송
        // 프론트에서 이보다 큰 메세지들의 읽지 않은 멤버 수에 -1 수행
        messagingTemplate.convertAndSend(
                "/topic/studies/" + studyId + "/unread", new LastReadMessageResponseDto(lastReadMessageId)
        );
    }

    @Override
    @Transactional
    public void closeChatModal(Long studyId, User currentUser) {

        modalManager.closeSocket(studyId, currentUser.getId());
    }

    @Override
    public void refreshModalState(Long studyId, User currentUser) {

        modalManager.refreshSocket(studyId, currentUser.getId());
    }

    // 모달을 열지 않은 사용자들에게 안읽은 메시지 수 전송을 비동기 처리
    @Async
    public void sendUnreadCountToClosedModalUsersAsync(Long studyId){

        sendUnreadCountToClosedModalUsers(studyId);
    }

    // 모달을 열지 않은 사용자들에게 안읽은 메시지 수 전송
    private void sendUnreadCountToClosedModalUsers(Long studyId) {
        List<StudyMember> allMembers = studyMemberRepository.findByStudyId(studyId);

        // 채팅방에 접속한 유저 목록을 한 번에 조회
        Set<Long> onlineUserIds = modalManager.getOpenSocketUserIds(studyId); // 메서드 내부에서 null을 반환하지 않도록 처리해야 함

        // 채팅방에 미접속 상태인 멤버들을 필터링
        List<StudyMember> offlineMembers = allMembers.stream()
                .filter(member -> !onlineUserIds.contains(member.getUser().getId()))
                .toList();

        // 미접속 멤버가 없다면 아무것도 하지 않고 종료
        if (offlineMembers.isEmpty()) {
            return;
        }

        // 미접속 멤버들의 LastReadMessage 정보를 <studyMemberId, LastReadMessageId> 형식으로 DB에서 한 번에 조회
        Map<Long, Long> lastReadMessageIdMap = lastReadRepository.findByStudyMemberIn(offlineMembers)
                .stream()
                .collect(Collectors.toMap(
                        lrm -> lrm.getStudyMember().getId(),
                        LastReadMessage::getLastReadMessageId
                ));

        // 5. 미접속 멤버들을 순회하며 알림 전송 로직 수행
        offlineMembers.forEach(member -> {
            User user = member.getUser();
            Long lastReadId = lastReadMessageIdMap.getOrDefault(member.getId(), 0L);
            long unreadCount = chatMessageRepository.countByIdGreaterThanAndStudyId(lastReadId, studyId);

            if (unreadCount > 0) {
                UnreadCountResponseDto response = new UnreadCountResponseDto(unreadCount);

                messagingTemplate.convertAndSendToUser(
                        user.getEmail(),
                        "/queue/studies/" + studyId + "/unread",
                        response
                );
            }
        });
    }

    private void validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
        }

        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_TOO_LONG);
        }
    }

    private StudyMember findAndValidateStudyMember(Long studyId, User currentUser) {
        if (!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }

        StudyMember member = studyMemberRepository.findByStudyIdAndUserId(studyId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return member;
    }

    private ChatReactionCountDto CountMessageReaction(ChatMessage message) {

        if (message.getId() == null) {
            return new ChatReactionCountDto(null, 0L, 0L);
        }

        List<ChatReactionCountDto> results = chatReactionRepository.findReactionCountsByMessageIdIn(List.of(message.getId()));

        return results.isEmpty() ? new ChatReactionCountDto(message.getId(), 0L, 0L) : results.getFirst();
    }
}
