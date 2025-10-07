package com.pado.domain.quiz.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizNotificationListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuizCompletedEvent(QuizCompletedEvent event) {
        log.info("Handling quiz completed event for studyId: {}", event.studyId());
        String message = "🎉 새로운 퀴즈 '%s'이(가) 생성되었습니다!".formatted(event.quizTitle());
        // 채팅 서비스 호출
    }
}