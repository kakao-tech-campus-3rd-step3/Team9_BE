package com.pado.domain.chat.service;

import com.pado.domain.chat.dto.request.ChatMessageRequestDto;
import com.pado.domain.chat.dto.response.ChatMessageListResponseDto;
import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.dto.response.LastReadMessageResponseDto;
import com.pado.domain.chat.dto.response.UnreadCountResponseDto;
import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.LastReadMessage;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                .sender(studyMember)
                .content(requestDto.content())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 현재 채팅방에 접속하고 있는 멤버 대상으로만 마지막으로 읽은 메세지 id 업데이트
        modalManager.getOpenModalUserIds(studyId).forEach(userId -> {
            studyMemberRepository.findByStudyIdAndUserId(studyId, userId)
                    .ifPresent(member -> {
                        LastReadMessage lastReadMessage = lastReadRepository.findByStudyMember(member)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LAST_READ_CHAT));

                        lastReadMessage.updateLastReadMessageId(savedMessage.getId());
                    });
        });

        // 스터디 전체 멤버 - 채팅방 접속 중인 멤버 방식으로 계산
        Long unreadMemberCount = lastReadRepository.countUnreadMembers(studyId, savedMessage.getId());

        // 채팅 전송
        ChatMessageResponseDto responseDto = ChatMessageResponseDto.from(savedMessage, unreadMemberCount);
        messagingTemplate.convertAndSend("/topic/studies/" + studyId + "/chats", responseDto);

        // 현재 채팅방에 접속하고 있지 않은 사용자들에게 안읽은 메시지 수 알림
        sendUnreadCountToClosedModalUsers(studyId);
    }

    @Override
    public ChatMessageListResponseDto getChatMessages(Long studyId, Long cursor, int size, User currentUser) {
        validateStudyMemberPermission(studyId, currentUser);

        List<ChatMessage> chatMessages = chatMessageRepository.findChatMessagesWithCursor(studyId, cursor, size);

        boolean hasNext = chatMessages.size() > size;
        if (hasNext) {
            chatMessages = chatMessages.subList(0, size);
        }

        List<Long> messageIds = chatMessages.stream()
                .map(ChatMessage::getId)
                .toList();

        Map<Long, Long> unreadCounts = lastReadRepository.getUnreadCountsForMessages(studyId, messageIds);

        List<ChatMessageResponseDto> messageDtos = chatMessages.stream()
                .map(msg-> ChatMessageResponseDto.from(
                        msg,
                        unreadCounts.getOrDefault(msg.getId(), 0L)
                ))
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

    @Override
    @Transactional
    public void openChatModal(Long studyId, User currentUser) {
        validateStudyMemberPermission(studyId, currentUser);

        modalManager.openModal(studyId, currentUser.getId());

        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        // 현재 채팅방에서 가장 최신 메시지 아이디 추출
        Optional<ChatMessage> latestMessage = chatMessageRepository.findTopByStudyIdOrderByIdDesc(studyId);
        long lastestChatMessageId = latestMessage.isPresent() ? latestMessage.get().getId() : 0L;

        // 모달을 킨 유저가 가장 마지막으로 읽었던 메세지 아이디 추출
        LastReadMessage lastReadMessage = lastReadRepository.findByStudyMember(studyMember)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LAST_READ_CHAT));
        long lastReadMessageId = lastReadMessage.getLastReadMessageId();

        lastReadMessage.updateLastReadMessageId(lastestChatMessageId);

        // 모달을 킨 유저가 가장 마지막으로 읽었던 메세지 아이디 전송
        // 프론트에서 이보다 큰 메세지들의 읽지 않은 멤버 수에 -1 수행
        messagingTemplate.convertAndSend(
                "/topic/studies/" + studyId + "/unread", new LastReadMessageResponseDto(lastReadMessageId)
        );
    }

    @Override
    @Transactional
    public void closeChatModal(Long studyId, User currentUser) {
        validateStudyMemberPermission(studyId, currentUser);

        modalManager.closeModal(studyId, currentUser.getId());
    }

    @Override
    public void refreshModalState(Long studyId, User currentUser) {
        validateStudyMemberPermission(studyId, currentUser);

        modalManager.refreshModal(studyId, currentUser.getId());
    }

    // 모달을 열지 않은 사용자들에게 안읽은 메시지 수 전송
    private void sendUnreadCountToClosedModalUsers(Long studyId) {
        List<StudyMember> allMembers = studyMemberRepository.findByStudyId(studyId);

        allMembers.forEach(member -> {
            User user = member.getUser();

            if (!modalManager.isModalOpen(studyId, user.getId())) {

                LastReadMessage lastRead = lastReadRepository.findByStudyMember(member)
                        .orElse(LastReadMessage.builder()
                                .studyMember(member)
                                .lastReadMessageId(0L)
                                .build());

                long unreadCount = chatMessageRepository.countByIdGreaterThanAndStudyId(lastRead.getLastReadMessageId(), studyId);

                if (unreadCount > 0) {
                    UnreadCountResponseDto response = new UnreadCountResponseDto(unreadCount);

                    messagingTemplate.convertAndSendToUser(
                            user.getEmail(),
                            "/queue/studies/" + studyId + "/unread",
                            response
                    );
                }
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

    private void validateStudyMemberPermission(Long studyId, User currentUser) {
        if (!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }

        if (!studyMemberRepository.existsByStudyIdAndUserId(studyId, currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }
    }
}