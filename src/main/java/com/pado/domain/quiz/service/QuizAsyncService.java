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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAsyncService {

    private final QuizRepository quizRepository;
    private final GeminiClient geminiClient;
    private final QuizTransactionService quizTransactionService;
    private final FileProcessingService fileProcessingService;
    private final Executor quizThreadPool;

    private static final int SHORT_TEXT_THRESHOLD = 500;
    private static final int MINIMUM_SUCCESSFUL_QUESTIONS = 4;

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

            // 문제 수 결정 & 힌트 생성
            int questionCount = calculateQuestionCount(combinedText.length());
            List<String> hints = buildHints(combinedText.length(), questionCount);
            log.info("QuizId: {}. Preparing to generate {} questions.", quizId, questionCount);

            // AI 퀴즈 생성
            List<AiQuestionDto> successfulQuestions = generateQuestionsInParallel(combinedText, hints);
            if (successfulQuestions.size() < MINIMUM_SUCCESSFUL_QUESTIONS) {
                throw new BusinessException(ErrorCode.API_RESPONSE_INVALID, "AI failed to generate sufficient questions.");
            }

            // 퀴즈 제한 시간 생성 & AiQuizResponseDto로 변환
            int totalRecommendedTime = calculateTotalTimeWithGuards(successfulQuestions);
            AiQuizResponseDto finalQuizDto = new AiQuizResponseDto(totalRecommendedTime, successfulQuestions);

            // AI 퀴즈 검증
            validateAiResponse(finalQuizDto, quizId);

            // 생성된 퀴즈 저장
            try {
                quizTransactionService.saveSuccessfulQuiz(quizId, finalQuizDto);
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.QUIZ_NOT_FOUND) {
                    log.info("[Async Task] Quiz '{}' was deleted during generation. Task gracefully terminated.", quizId);
                } else {
                    throw e;
                }
            }

        }, quizThreadPool).exceptionally(ex -> {
            log.error("Async task for quizId {} failed.", quizId, ex);
            quizTransactionService.updateQuizStatusToFailed(quizId);
            return null;
        });
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

    private List<String> buildHints(int textLength, int questionCount) {
        if (textLength < SHORT_TEXT_THRESHOLD) {
            return Collections.nCopies(questionCount, null);
        }

        return IntStream.range(0, questionCount)
                .mapToObj(i -> String.format("이 문서의 %d/%d 부분에 집중해서 문제를 만들어 줘.", i + 1, questionCount))
                .toList();
    }

    private List<AiQuestionDto> generateQuestionsInParallel(String text, List<String> hints) {
        // hint 리스트를 순회하면서 AI 문제 생성 요청
        List<CompletableFuture<AiQuestionDto>> futures = hints.stream()
                .map(hint -> geminiClient.generateSingleQuestion(text, hint, quizThreadPool)
                    .exceptionally(ex -> {
                                log.warn("Single-question generation failed; skipping this item.", ex);
                                return null;
                    }))
                .toList();

        // 모든 작업이 끝날 때까지 기다림 -> 성공한 결과만 모아서 반환
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .join();
    }

    private int calculateTotalTimeWithGuards(List<AiQuestionDto> questions) {
        int totalTime = 0;
        for (AiQuestionDto question : questions) {
            int baseTime = "MULTIPLE_CHOICE".equals(question.questionType()) ? 40 : 20;
            totalTime += baseTime;
        }

        totalTime = Math.max(totalTime, 120);
        totalTime = Math.min(totalTime, 600);

        return totalTime;
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
