package com.pado.domain.quiz.service;

import com.pado.domain.material.entity.File;
import com.pado.domain.material.entity.ProcessingStatus;
import com.pado.domain.quiz.entity.Quiz;
import com.pado.domain.quiz.repository.QuizRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.infrastruture.ai.dto.AiQuestionDto;
import com.pado.infrastruture.ai.dto.AiQuizResponseDto;
import com.pado.infrastruture.ai.impl.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAsyncService {

    private final QuizRepository quizRepository;
    private final GeminiClient geminiClient;
    private final QuizTransactionService quizTransactionService;
    private final FileProcessingService fileProcessingService;
    private final Executor quizThreadPool;

    public CompletableFuture<Void> processAndCallAiInBackground(Long quizId) {
        return CompletableFuture.runAsync(() -> {
            log.info("[Async Task] Starting for quizId: {}", quizId);

            Quiz quiz = quizRepository.findWithSourceFilesById(quizId)
                    .orElse(null);

            if (quiz == null) {
                log.info("[Async Task] Quiz '{}' was deleted before generation could start. Task cancelled.", quizId);
                return;
            }

            // 파일 텍스트 추출
            String combinedText = getAndProcessTextForFiles(quiz.getSourceFiles());
            if (combinedText.isBlank()) {
                throw new BusinessException(ErrorCode.FILE_PROCESSING_FAILED, "Extracted text is blank.");
            }

            int questionCount = calculateQuestionCount(combinedText.length());

            // AI 퀴즈 생성 요청 & 검증
            AiQuizResponseDto aiQuizDto = geminiClient.generateQuiz(combinedText);
            validateAiResponse(aiQuizDto, quizId);

            // 생성된 퀴즈 저장
            try {
                quizTransactionService.saveSuccessfulQuiz(quizId, aiQuizDto);
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.QUIZ_NOT_FOUND) {
                    log.info("[Async Task] Quiz '{}' was deleted during generation. Task gracefully terminated.", quizId);
                } else {
                    throw e;
                }
            }

        }, quizThreadPool);
    }

    private String getAndProcessTextForFiles(Set<File> files) {
        return files.stream()
                .map(file -> {
                    if (file.getProcessingStatus() == ProcessingStatus.COMPLETED) {
                        return file.getExtractedText();
                    }
                    return fileProcessingService.processFileAndUpdateState(file.getId());
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private int calculateQuestionCount(int textLength) {
        int count = textLength / 300;
        return Math.max(5, Math.min(10, count));
    }

    private void validateAiResponse(AiQuizResponseDto aiQuizDto, Long quizId) {
        // 생성된 문제가 비어있는 경우
        if (aiQuizDto == null || aiQuizDto.questions() == null || aiQuizDto.questions().isEmpty()) {
            throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "AI returned no questions for quizId: " + quizId);
        }

        // 전체 질문 개수 범위 (5~10) 체크
        int questionCount = aiQuizDto.questions().size();
        if (questionCount < 5 || questionCount > 10) {
            throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "Number of questions out of range (5~10) for quizId: " + quizId);
        }

        // 권장 시간 존재 여부
        if (aiQuizDto.recommendedTimeLimitSeconds() == null || aiQuizDto.recommendedTimeLimitSeconds() <= 0) {
            throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "Recommended time limit missing or invalid for quizId: " + quizId);
        }

        for (AiQuestionDto questionDto : aiQuizDto.questions()) {
            // 질문의 텍스트 존재 여부
            if (questionDto.questionText() == null || questionDto.questionText().isBlank()) {
                throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "AI returned a question with no text for quizId: " + quizId);
            }

            // 해설 존재 여부
            if (questionDto.explanation() == null || questionDto.explanation().isBlank()) {
                throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "Explanation missing for question in quizId: " + quizId);
            }

            // 질문 타입 유효성
            String type = questionDto.questionType();
            if (!"MULTIPLE_CHOICE".equals(type) && !"SHORT_ANSWER".equals(type)) {
                throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "Unknown question type: " + type + " for quizId: " + quizId);
            }

            if ("MULTIPLE_CHOICE".equals(type)) {
                // 객관식 옵션 개수 (2~4) 체크
                if (questionDto.options() == null || questionDto.options().size() < 2 || questionDto.options().size() > 4) {
                    throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "MCQ options count invalid for quizId: " + quizId);
                }

                // 객관식 정답 인덱스 유효성 체크
                Integer correctIdx = questionDto.correctAnswerIndex();
                if (correctIdx == null || correctIdx < 0 || correctIdx >= questionDto.options().size()) {
                    throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "MCQ correct answer index invalid for quizId: " + quizId);
                }

            } else {
                // 단답형 문제의 답이 없는 경우
                if (questionDto.sampleAnswer() == null || questionDto.sampleAnswer().isBlank()) {
                    throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "SAQ has no sample answer for quizId: " + quizId);
                }
            }
        }
    }
}
