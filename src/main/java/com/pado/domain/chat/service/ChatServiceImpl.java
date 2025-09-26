package com.pado.domain.chat.service;

import com.pado.domain.chat.dto.request.ChatMessageRequestDto;
import com.pado.domain.chat.dto.response.ChatMessageListResponseDto;
import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.entity.StudyStatus;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;

    @Override
    @Transactional
    public ChatMessageResponseDto sendMessage(Long studyId, ChatMessageRequestDto requestDto, User currentUser) {

        // 인터셉터에서 1차 검증, 서비스 로직에서 2차 검증
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

        return ChatMessageResponseDto.from(savedMessage);
    }

    @Override
    public ChatMessageListResponseDto getChatMessages(Long studyId, Long cursor, int size, User currentUser) {
        // 2차 검증
        validateStudyMemberPermission(studyId, currentUser);

        List<ChatMessage> chatMessages = chatMessageRepository.findChatMessagesWithCursor(studyId, cursor, size);

        boolean hasNext = chatMessages.size() > size;
        if (hasNext) {
            chatMessages = chatMessages.subList(0, size);
        }

        List<ChatMessageResponseDto> messageDtos = chatMessages.stream()
                .map(ChatMessageResponseDto::from)
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

    private void validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
        }

        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_TOO_LONG);
        }
    }

    public void validateStudyMemberPermission(Long studyId, User currentUser) {
        if (!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }

        if (!studyMemberRepository.existsByStudyIdAndUserId(studyId, currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }
    }
}
