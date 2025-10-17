package com.pado.domain.quiz.service;

import com.pado.domain.quiz.dto.projection.QuizInfoProjection;
import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.dto.request.AnswerRequestDto;
import com.pado.domain.quiz.dto.response.*;
import com.pado.domain.quiz.entity.*;
import com.pado.domain.quiz.repository.AnswerSubmissionRepository;
import com.pado.domain.quiz.repository.QuizRepository;
import com.pado.domain.quiz.repository.QuizSubmissionRepository;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.study.service.StudyRankingService;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final AnswerSubmissionRepository answerSubmissionRepository;
    private final StudyRankingService rankPointService;
    private final QuizGenerationService quizGenerationService;
    private final Clock clock;

    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public CursorResponseDto<QuizInfoDto> findQuizzesByStudy(Long studyId, User user, Long cursor, int pageSize) {
        // 1. 스터디 존재 & 권한 확인
        validateMember(studyId, user.getId());

        // 2. 페이지 사이즈 조정
        int adjustedSize = clampPageSize(pageSize);

        // 3. 퀴즈 조회(hasNext 계산을 위해 +1개 조회)
        List<QuizInfoProjection> projections = quizRepository.findByStudyIdWithCursor(studyId, cursor, adjustedSize + 1);

        // 4. hasNext 계산 & 반환 개수만큼 자르기
        boolean hasNext = projections.size() > adjustedSize;
        List<QuizInfoProjection> contentProjections = hasNext
                                                    ? projections.subList(0, adjustedSize)
                                                    : projections;

        // 5. 퀴즈 ID 추출
        List<Long> quizIds = contentProjections.stream()
                                                .map(QuizInfoProjection::quizId)
                                                .toList();

        // 6. 퀴즈별 문항 개수 & 상태 조회
        Map<Long, Long> questionCountMap = quizRepository.findQuestionCountsByQuizIds(quizIds);
        Map<Long, SubmissionStatusDto> submissionInfoMap = fetchSubmissionInfo(quizIds, user.getId());

        // 7. dto로 매핑
        List<QuizInfoDto> dtos = mapToDtos(contentProjections, questionCountMap, submissionInfoMap);

        // 8. nextCursor 계산
        Long nextCursor = calculateNextCursor(dtos, hasNext);

        return new CursorResponseDto<>(dtos, nextCursor, hasNext);
    }

    public QuizDetailDto getQuizDetail(Long quizId, User user) {
        // 1. 퀴즈 조회
        Quiz quiz = quizRepository.findDetailById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 2. 사용자 권한 확인
        if (!studyMemberRepository.existsByStudyIdAndUserId(quiz.getStudy().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        // 3. Dto에 매핑
        return mapToQuizDetailDto(quiz);
    }

    @Transactional
    public QuizProgressDto startQuiz(Long quizId, User user) {
        // 1. 퀴즈 존재 확인
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 2. 사용자 권한 확인
        if (!studyMemberRepository.existsByStudyIdAndUserId(quiz.getStudy().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        // 3. 기존 제출 기록 조회 또는 새로 생성
        QuizSubmission submission = findOrCreateSubmission(quiz, user);

        return createQuizProgressDto(submission);
    }

    @Transactional
    public QuizResultDto completeQuiz(Long submissionId, User user, List<AnswerRequestDto> answers) {
        // 1. Submission 조회
        QuizSubmission submission = quizSubmissionRepository.findWithDetailsById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND));

        // 2. 권한 & 상태 확인
        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        submission.validateIsNotCompleted();

        // 3. 점수 계산
        int finalScore = gradeAnswers(submission, answers);

        // 4. submission 갱신
        submission.complete(finalScore);

        // 5. rank point 갱신
        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(submission.getQuiz().getStudy(), user)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));
        studyMember.addRankPoints(finalScore);
        rankPointService.addPointsFromQuiz(studyMember, submission);

        return mapToResultDto(submission);
    }

    @Transactional
    public void requestQuizRegeneration(Long quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        validateMember(quiz.getStudy().getId(), user.getId());

        if (quiz.getStatus() == QuizStatus.FAILED) {
            quiz.updateStatus(QuizStatus.GENERATING);
            quizGenerationService.processAndCallAiInBackground(quiz.getId());
        } else {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_PROCESSED);
        }
    }

    @Transactional
    public void deleteQuiz(Long quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        StudyMember member = studyMemberRepository.findByStudyAndUser(quiz.getStudy(), user)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        if (member.getRole() != StudyMemberRole.LEADER) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS, "삭제 권한이 없습니다.");
        }
        rankPointService.revokePointsForQuiz(quizId);
        quizRepository.delete(quiz);
    }

    private QuizSubmission findOrCreateSubmission(Quiz quiz, User user) {
        return quizSubmissionRepository
                .findByQuizIdAndUserId(quiz.getId(), user.getId())
                .map(submission -> {
                    submission.validateIsNotCompleted();
                    return submission;
                })
                .orElseGet(() -> createNewSubmission(quiz, user));
    }

    private QuizSubmission createNewSubmission(Quiz quiz, User user) {
        QuizSubmission newSubmission = QuizSubmission.builder()
                .quiz(quiz)
                .user(user)
                .status(SubmissionStatus.IN_PROGRESS)
                .submittedAt(LocalDateTime.now(clock))
                .build();
        return quizSubmissionRepository.save(newSubmission);
    }

    private int gradeAnswers(QuizSubmission submission, List<AnswerRequestDto> answers) {
        int score = 0;

        Map<Long, QuizQuestion> questionMap = submission.getQuiz().getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        for (AnswerRequestDto userAnswerDto : answers) {
            QuizQuestion question = questionMap.get(userAnswerDto.questionId());
            if (question == null) continue;

            boolean isCorrect = checkAnswer(question, userAnswerDto.userAnswer());
            if (isCorrect) {
                score++;
            }

            AnswerSubmission answerSubmission = AnswerSubmission.builder()
                    .question(question)
                    .submittedAnswer(userAnswerDto.userAnswer())
                    .isCorrect(isCorrect)
                    .build();

            submission.addAnswerSubmission(answerSubmission);
        }

        return score;
    }

    private boolean checkAnswer(QuizQuestion question, String userAnswer) {
        if (userAnswer == null || userAnswer.isBlank()) {
            return false;
        }

        if (question instanceof MultipleChoiceQuestion mcq) {
            return mcq.getCorrectChoice() != null &&
                    mcq.getCorrectChoice()
                            .getId()
                            .toString()
                            .equals(userAnswer);
        } else if (question instanceof ShortAnswerQuestion saq) {
            return saq.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());
        }

        return false;
    }

    private QuizResultDto mapToResultDto(QuizSubmission submission) {
        Map<Long, AnswerSubmission> userAnswersMap = submission.getAnswers().stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer
                ));

        List<AnswerResultDto> results = submission.getQuiz().getQuestions().stream()
                .map(question -> {
                    AnswerSubmission userAnswer = userAnswersMap.get(question.getId());
                    return mapToAnswerResultDto(question, userAnswer);
                })
                .toList();

        return new QuizResultDto(
                submission.getId(),
                submission.getScore(),
                submission.getQuiz().getQuestions().size(),
                results
        );
    }

    private AnswerResultDto mapToAnswerResultDto(QuizQuestion question, AnswerSubmission userAnswer) {
        String userAnswerText = (userAnswer != null)
                                ? userAnswer.getSubmittedAnswer()
                                : null;

        boolean isCorrect = (userAnswer != null)
                            ? userAnswer.isCorrect()
                            : false;

        String correctAnswer = "";
        List<ChoiceResultDto> choices = Collections.emptyList();

        if (question instanceof MultipleChoiceQuestion mcq) {
            correctAnswer = mcq.getCorrectChoice() != null
                            ? mcq.getCorrectChoice().getId().toString()
                            : null;

            choices = mcq.getChoices().stream()
                    .map(choice -> new ChoiceResultDto(
                            choice.getId(),
                            choice.getChoiceText(),
                            choice.getId().equals(mcq.getCorrectChoice() != null
                                                    ? mcq.getCorrectChoice().getId()
                                                    : null),
                            choice.getId().toString().equals(userAnswerText)
                    ))
                    .toList();
        } else if (question instanceof ShortAnswerQuestion saq) {
            correctAnswer = saq.getAnswer();
        }

        return new AnswerResultDto(
                question.getId(),
                question.getQuestionText(),
                isCorrect,
                userAnswerText,
                correctAnswer,
                choices
        );
    }

    private QuizProgressDto createQuizProgressDto(QuizSubmission submission) {
        // 1. 현재 submission의 quiz 가져오기
        Quiz quiz = submission.getQuiz();

        // 2. 사용자가 제출한 모든 answerSubmission 불러오기
        Map<Long, String> userAnswerMap = answerSubmissionRepository.findBySubmissionId(submission.getId()).stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        AnswerSubmission::getSubmittedAnswer
                ));

        // 3. 퀴즈 문제별 DTO 생성
        List<QuestionProgressDto> questionDtos = quiz.getQuestions().stream()
                .map(question -> mapToQuestionProgressDto(question, userAnswerMap.get(question.getId())))
                .toList();

        // 4. 남은 시간 계산
        Long remainingSeconds = calculateRemainingSeconds(quiz.getTimeLimitSeconds(), submission.getSubmittedAt());

        return new QuizProgressDto(
                submission.getId(),
                quiz.getTitle(),
                quiz.getTimeLimitSeconds(),
                remainingSeconds,
                questionDtos
        );

    }

    private Long calculateRemainingSeconds(Integer timeLimitSeconds, LocalDateTime startTime) {
        if (timeLimitSeconds == null || timeLimitSeconds <= 0) {
            return null;
        }

        if (startTime == null) {
            return timeLimitSeconds.longValue();
        }

        long secondsElapsed = Duration.between(startTime, LocalDateTime.now(clock)).getSeconds();
        long secondsRemaining = timeLimitSeconds - secondsElapsed;

        return Math.max(0, secondsRemaining);
    }

    private QuestionProgressDto mapToQuestionProgressDto(QuizQuestion question, String userAnswer) {
        String questionType = "";
        List<ChoiceDto> choices = Collections.emptyList();

        if (question instanceof MultipleChoiceQuestion mcq) {
            questionType = "MULTIPLE_CHOICE";
            choices = mcq.getChoices().stream()
                    .map(choice -> new ChoiceDto(choice.getId(), choice.getChoiceText()))
                    .toList();
        } else if (question instanceof ShortAnswerQuestion) {
            questionType = "SHORT_ANSWER";
        }

        return new QuestionProgressDto(
                question.getId(),
                questionType,
                question.getQuestionText(),
                choices,
                userAnswer
        );
    }

    private QuizDetailDto mapToQuizDetailDto(Quiz quiz) {
        List<QuestionDetailDto> questionDtos = quiz.getQuestions().stream()
                .map(this::mapToQuestionDetailDto)
                .toList();

        return new QuizDetailDto(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTimeLimitSeconds(),
                questionDtos
        );
    }

    private QuestionDetailDto mapToQuestionDetailDto(QuizQuestion question) {
        QuestionType questionType;
        List<ChoiceDto> choices = Collections.emptyList();

        if (question instanceof MultipleChoiceQuestion mcq) {
            questionType = QuestionType.MULTIPLE_CHOICE;
            choices = mcq.getChoices().stream()
                    .map(choice -> new ChoiceDto(choice.getId(), choice.getChoiceText()))
                    .toList();
        } else if (question instanceof ShortAnswerQuestion) {
            questionType = QuestionType.SHORT_ANSWER;
        } else {
            throw new BusinessException(ErrorCode.UNSUPPORTED_QUESTION_TYPE,
                    "지원하지 않는 문제 타입: " + question.getClass().getSimpleName());
        }

        return new QuestionDetailDto(
                question.getId(),
                questionType,
                question.getQuestionText(),
                choices
        );
    }

    private void validateMember(Long studyId, Long userId) {
        if (!studyMemberRepository.existsByStudyIdAndUserId(studyId, userId)) {
            if (!studyRepository.existsById(studyId)) {
                throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }
    }

    private int clampPageSize(int size) {
        if (size <= 0) return DEFAULT_PAGE_SIZE;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private Map<Long, SubmissionStatusDto> fetchSubmissionInfo(List<Long> quizIds, Long userId) {
        if (quizIds.isEmpty()) return Map.of();

        return quizSubmissionRepository.findSubmissionStatuses(quizIds, userId)
                .stream()
                .collect(Collectors.toMap(
                        SubmissionStatusDto::quizId,
                        dto -> dto
                ));
    }

    private List<QuizInfoDto> mapToDtos(List<QuizInfoProjection> projections, Map<Long, Long> questionCountMap, Map<Long, SubmissionStatusDto> infoMap) {
        return projections.stream()
                .map(proj -> QuizInfoDto.of(
                        proj,
                        questionCountMap.getOrDefault(proj.quizId(), 0L).intValue(),
                        infoMap.get(proj.quizId())
                ))
                .toList();
    }

    private Long calculateNextCursor(List<QuizInfoDto> dtos, boolean hasNext) {
        if (!hasNext || dtos.isEmpty()) return null;
        return dtos.get(dtos.size() - 1).getCursorId();
    }
}