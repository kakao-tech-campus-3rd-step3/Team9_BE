package com.pado.domain.chat.listener;

import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.MessageType;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.material.event.NoticeCreatedEvent;
import com.pado.domain.schedule.event.ScheduleCreatedEvent;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.global.config.AppConfig;
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
    private final AppConfig appConfig;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handleNoticeCreatedEvent(NoticeCreatedEvent event) {

        Study study = studyRepository.findById(event.studyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        String systemMessageContent = "새로운 공지사항이 등록되었습니다: " + event.title();
        String link = baseUrl + "/study/document/" + event.noticeId();

        ChatMessage systemMessage = ChatMessage.builder()
                .study(study)
                .sender(null)
                .content(systemMessageContent)
                .type(MessageType.NOTICE)
                .link(link)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(systemMessage);

        ChatMessageResponseDto responseDto = ChatMessageResponseDto.from(
                savedMessage, 
                0L,
                0L,
                0L
        );
        
        messagingTemplate.convertAndSend("/topic/studies/" + event.studyId() + "/chats", responseDto);
        log.info("공지사항 알림 메시지 전송 완료: studyId={}, link={}", event.studyId(), link);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handleScheduleCreatedEvent(ScheduleCreatedEvent event) {

        Study study = studyRepository.findById(event.studyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        String systemMessageContent = "새로운 일정이 등록되었습니다: " + event.title();
        String link = baseUrl + "/study/schedule/tune";

        ChatMessage systemMessage = ChatMessage.builder()
                .study(study)
                .sender(null)
                .content(systemMessageContent)
                .type(MessageType.SCHEDULE)
                .link(link)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(systemMessage);

        ChatMessageResponseDto responseDto = ChatMessageResponseDto.from(
                savedMessage,
                0L,
                0L,
                0L
        );

        messagingTemplate.convertAndSend("/topic/studies/" + event.studyId() + "/chats", responseDto);
        log.info("일정 알림 메시지 전송 완료: studyId={}, link={}", event.studyId(), link);
    }
}
