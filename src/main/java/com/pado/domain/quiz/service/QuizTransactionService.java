package com.pado.domain.quiz.service;

import com.pado.domain.quiz.entity.*;
import com.pado.domain.quiz.event.QuizCompletedEvent;
import com.pado.domain.quiz.repository.QuizRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.infrastruture.ai.dto.AiQuestionDto;
import com.pado.infrastruture.ai.dto.AiQuizResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizTransactionService {

    private final QuizRepository quizRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void saveSuccessfulQuiz(Long quizId, AiQuizResponseDto aiQuizDto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        List<QuizQuestion> newQuestions = aiQuizDto.questions().stream()
                .map(dto -> mapDtoToQuestion(quiz, dto))
                .toList();

        quiz.addQuestions(newQuestions);
        quiz.setTimeLimitSeconds(aiQuizDto.recommendedTimeLimitSeconds());
        quiz.updateStatus(QuizStatus.ACTIVE);
        log.info("Successfully generated and saved quiz for quizId: {}", quiz.getId());
        eventPublisher.publishEvent(new QuizCompletedEvent(quiz.getStudy().getId(), quiz.getTitle(), quiz.getId()));

    }

    @Transactional
    public void updateQuizStatusToFailed(Long quizId) {
        quizRepository.findById(quizId).ifPresent(quiz -> {
            if (quiz.getStatus() == QuizStatus.GENERATING) {
                quiz.updateStatus(QuizStatus.FAILED);
            }
        });
    }

    private QuizQuestion mapDtoToQuestion(Quiz quiz, AiQuestionDto dto) {
        // 객관식일 때
        if ("MULTIPLE_CHOICE".equals(dto.questionType())) {
            MultipleChoiceQuestion question = MultipleChoiceQuestion.builder()
                    .quiz(quiz)
                    .questionText(dto.questionText())
                    .explanation(dto.explanation())
                    .build();

            List<QuizChoice> choices = dto.options().stream()
                    .map(text -> QuizChoice.builder()
                            .question(question)
                            .choiceText(text)
                            .build()
                    )
                    .toList();
            question.setChoices(choices);

            if (dto.correctAnswerIndex() != null && dto.correctAnswerIndex() < choices.size()) {
                question.setCorrectChoice(choices.get(dto.correctAnswerIndex()));
            }
            return question;
            // 주관식일 때
        } else {
            return ShortAnswerQuestion.builder()
                    .quiz(quiz)
                    .questionText(dto.questionText())
                    .explanation(dto.explanation())
                    .answer(dto.sampleAnswer())
                    .build();
        }
    }
}