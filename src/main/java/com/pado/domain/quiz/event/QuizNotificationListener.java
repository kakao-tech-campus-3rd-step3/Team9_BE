package com.pado.domain.quiz.event;

import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.MessageType;
import com.pado.domain.chat.repository.ChatMessageRepository;
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
public class QuizNotificationListener {

    @Value("${app.frontend.url}")
    private String baseUrl;

    private String frontendUrl;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final StudyRepository studyRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuizCompletedEvent(QuizCompletedEvent event) {
        log.info("Handling quiz completed event for studyId: {}", event.studyId());
        String message = "🎉 새로운 퀴즈 '%s'이(가) 생성되었습니다!".formatted(event.quizTitle());
        String link = baseUrl + "/study/quiz";
        // 채팅 서비스 호출
        sendSystemMessage(event.studyId(), message, link, MessageType.QUIZ);
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