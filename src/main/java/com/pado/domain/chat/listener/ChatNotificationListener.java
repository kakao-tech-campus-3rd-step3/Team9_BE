package com.pado.domain.chat.listener;

import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.MessageType;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.material.event.NoticeCreatedEvent;
import com.pado.domain.schedule.event.ScheduleCreatedEvent;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatNotificationListener {

    @Value("${app.frontend.url}")
    private String baseUrl;

    private String frontendUrl;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final StudyRepository studyRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNoticeCreatedEvent(NoticeCreatedEvent event) {

        String systemMessageContent = "새로운 공지사항이 등록되었습니다: " + event.title();
        String link = baseUrl + "/study/document/" + event.noticeId();

        sendSystemMessage(event.studyId(), systemMessageContent, link, MessageType.NOTICE);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleScheduleCreatedEvent(ScheduleCreatedEvent event) {

        String systemMessageContent = "새로운 일정이 등록되었습니다: " + event.title();
        String link = baseUrl + "/study/schedule/tune";

        sendSystemMessage(event.studyId(), systemMessageContent, link, MessageType.SCHEDULE);

    }

    private void sendSystemMessage(Long studyId, String content, String link, MessageType type) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        ChatMessage systemMessage = ChatMessage.builder()
                .study(study)
                .sender(null)
                .content(content)
                .type(type)
                .link(link)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(systemMessage);

        ChatMessageResponseDto responseDto = ChatMessageResponseDto.from(
                savedMessage,
                0L,
                0L,
                0L
        );

        messagingTemplate.convertAndSend("/topic/studies/" + studyId + "/chats", responseDto);
        log.info("{} 알림 메시지 전송 완료: studyId={}, link={}", type.name(), studyId, link);
    }
}
