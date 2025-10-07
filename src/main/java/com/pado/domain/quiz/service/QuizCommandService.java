package com.pado.domain.quiz.service;

import com.pado.domain.quiz.dto.request.AnswerRequestDto;
import com.pado.domain.quiz.dto.response.*;
import com.pado.domain.quiz.entity.*;
import com.pado.domain.quiz.mapper.QuizDtoMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizCommandService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final StudyRankingService rankPointService;
    private final QuizCreationService quizCreationService;
    private final QuizTransactionService quizTransactionService;
    private final QuizAsyncService quizAsyncService;
    private final QuizDtoMapper quizDtoMapper;

    public void requestQuizGeneration(User creator, String title, List<Long> fileIds, Long studyId) {
        // 사용자 권한 확인
        validateMember(studyId, creator.getId());

        // 퀴즈 객체 생성 & DB 저장
        Long quizId = quizCreationService.createQuizRecord(creator, title, fileIds, studyId);

        // 비동기 퀴즈 생성 호출
        invokeAiQuizGeneration(quizId);
    }

    @Transactional
    public void requestQuizRegeneration(Long quizId, User user) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findWithLockById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 권한 검증(해당 스터디 멤버)
        validateMember(quiz.getStudy().getId(), user.getId());

        // 퀴즈 상태가 FAILED일 때만 재생성
        if (quiz.getStatus() == QuizStatus.FAILED) {
            quiz.updateStatus(QuizStatus.GENERATING);
            invokeAiQuizGeneration(quizId);
        } else {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_PROCESSED);
        }
    }

    @Transactional
    public QuizProgressDto startQuiz(Long quizId, User user) {
        // 1. 퀴즈 존재 & 상태 확인
        Quiz quiz = quizRepository.findForStartQuizById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        quiz.validateIsActive();

        // 2. 사용자 권한 확인
        if (!studyMemberRepository.existsByStudyIdAndUserId(quiz.getStudy().getId(), user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        // 3. 기존 제출 기록 조회 또는 새로 생성
        QuizSubmission submission = quiz.start(user);
        quizSubmissionRepository.save(submission);

        return quizDtoMapper.toQuizProgressDto(quiz, submission);
    }

    private void validateMember(Long studyId, Long userId) {
        if (!studyMemberRepository.existsByStudyIdAndUserId(studyId, userId)) {
            if (!studyRepository.existsById(studyId)) {
                throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }
    }

    private void invokeAiQuizGeneration(Long quizId) {
        quizAsyncService.processAndCallAiInBackground(quizId)
                .orTimeout(200, TimeUnit.SECONDS)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Async task for quizId {} failed.", quizId, ex);
                        quizTransactionService.updateQuizStatusToFailed(quizId);
                    } else {
                        log.info("Async task for quizId {} completed successfully.", quizId);
                    }
                });
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
}