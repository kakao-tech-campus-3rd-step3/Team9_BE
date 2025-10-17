package com.pado.domain.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pado.domain.material.entity.File;
import com.pado.domain.material.entity.ProcessingStatus;
import com.pado.domain.material.repository.FileRepository;
import com.pado.domain.quiz.entity.*;
import com.pado.domain.quiz.repository.QuizRepository;
import com.pado.domain.s3.service.S3Service;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.infrastruture.ai.dto.AiQuestionDto;
import com.pado.infrastruture.ai.dto.AiQuizResponseDto;
import com.pado.infrastruture.ai.impl.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizGenerationService {

    private final StudyRepository studyRepository;
    private final FileRepository fileRepository;
    private final QuizRepository quizRepository;
    private final S3Service s3Service;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    @Transactional
    public void generateQuiz(User creator, String title, List<Long> fileIds, Long studyId) {
        // 1. 파일 ID 리스트 존재 유무 확인
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "퀴즈 생성을 위한 파일이 선택되지 않았습니다.");
        }

        // 2. 스터디 존재 유무 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        // 3. 파일 리스트 조회
        List<File> sourceFileList = fileRepository.findAllById(fileIds);
        if (sourceFileList.size() != fileIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 4. 새로운 퀴즈 객체 생성
        Quiz newQuiz = Quiz.builder()
                .study(study)
                .createdBy(creator)
                .title(title)
                .status(QuizStatus.GENERATING)
                .build();
        newQuiz.setSourceFiles(new HashSet<>(sourceFileList));

        // 5. 퀴즈 저장
        Quiz savedQuiz = quizRepository.save(newQuiz);

        // 6. 백그라운드에서 AI 퀴즈 생성
        processAndCallAiInBackground(savedQuiz.getId());
    }

    @Async
    @Transactional
    public void processAndCallAiInBackground(Long quizId) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        log.info("Starting background AI quiz generation for quizId: {}", quiz.getId());
        try {
            // 각 파일의 텍스트를 전처리
            String combinedText = getAndProcessTextForFiles(quiz.getSourceFiles());

            if (combinedText.isBlank()) {
                throw new BusinessException(ErrorCode.FILE_PROCESSING_FAILED,
                        "Extracted text is blank for quizId: " + quiz.getId());
            }

            // AI 문제 생성
            AiQuizResponseDto aiQuizDto = geminiClient.generateQuiz(combinedText);

            if (aiQuizDto.questions() == null || aiQuizDto.questions().isEmpty()) {
                throw new IllegalStateException("AI returned no questions for quizId: ".concat(quizId.toString()));
            }

            // 생성한 퀴즈 엔티티로 변환
            List<QuizQuestion> newQuestions = aiQuizDto.questions().stream()
                    .map(dto -> mapDtoToQuestion(quiz, dto))
                    .toList();

            quiz.addQuestions(newQuestions);
            quiz.setTimeLimitSeconds(aiQuizDto.recommendedTimeLimitSeconds());
            quiz.updateStatus(QuizStatus.ACTIVE);
            log.info("Successfully generated quiz for quizId: {}", quiz.getId());

        } catch (Exception e) {
            log.error("Failed to generate quiz for quizId: {}", quiz.getId(), e);
            quiz.updateStatus(QuizStatus.FAILED);
        } finally {
            // TODO: 사용자에게 알림 보내기
        }
    }

    private String getAndProcessTextForFiles(Set<File> files) {
        return files.stream()
                .map(file -> {
                    if (file.getProcessingStatus() == ProcessingStatus.COMPLETED) {
                        return file.getExtractedText();
                    }
                    return processFileNow(file);
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String processFileNow(File file) {
        try (InputStream inputStream = s3Service.downloadFileAsStream(file.getFileKey());
             BufferedInputStream bufferedStream = new BufferedInputStream(inputStream)) {

            Tika tika = new Tika();

            // 파일 타입 확인
            bufferedStream.mark(Integer.MAX_VALUE);
            String detectedMimeType = tika.detect(bufferedStream);
            bufferedStream.reset();

            // 허용된 타입인지 체크
            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT, "Disallowed MIME type: " + detectedMimeType);
            }

            // 텍스트 추출 & 전처리
            String extractedText = tika.parseToString(bufferedStream);
            String cleanedText = extractedText.replaceAll("\\s+", " ").trim();

            // 파일 상태 업데이트
            file.markAsCompleted(cleanedText, detectedMimeType);

            return cleanedText;

        } catch (Exception e) {
            file.markAsFailed();
            log.error("Failed to process file ID {}: {}", file.getId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR,
                    "File processing failed for fileId: " + file.getId());
        }
    }

    private QuizQuestion mapDtoToQuestion(Quiz quiz, AiQuestionDto dto) {
        // 객관식일 때
        if ("MULTIPLE_CHOICE".equals(dto.questionType())) {
            MultipleChoiceQuestion question = MultipleChoiceQuestion.builder()
                    .quiz(quiz)
                    .questionText(dto.questionText())
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
                    .answer(dto.sampleAnswer())
                    .build();
        }
    }
}