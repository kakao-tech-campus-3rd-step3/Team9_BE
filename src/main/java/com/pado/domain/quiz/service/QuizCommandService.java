package com.pado.domain.quiz.service;

import com.pado.domain.quiz.dto.request.AnswerRequestDto;
import com.pado.domain.quiz.dto.response.*;
import com.pado.domain.quiz.entity.*;
import com.pado.domain.quiz.mapper.QuizDtoMapper;
import com.pado.domain.quiz.repository.QuizRepository;
import com.pado.domain.quiz.repository.QuizSubmissionRepository;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final RankPointService rankPointService;
    private final QuizTransactionService quizTransactionService;
    private final QuizAsyncService quizAsyncService;
    private final QuizCreationService quizCreationService;
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

    @Transactional
    public void saveInProgressAnswers(Long submissionId, User user, List<AnswerRequestDto> answers) {
        // 1. Submission 조회
        QuizSubmission submission = quizSubmissionRepository.findForInProgressById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND));

        // 2. 권한 & 상태 확인
        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        submission.validateIsNotCompleted();

        // 3. 답안 유효성 검증
        validateAnswers(submission.getQuiz(), answers);

        // 4. 퀴즈에 속한 모든 문제들을 <문제ID, 문제 객체>로 변환
        Map<Long, QuizQuestion> questionMap = submission.getQuiz().getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        // 5. 사용자가 새로 보낸 답안들을 <문제 ID, 사용자가 입력한 답>으로 변환, 중복 값은 새로운 값으로 덮어 씀
        Map<Long, String> userAnswerMap = answers.stream()
                .collect(Collectors.toMap(
                        AnswerRequestDto::questionId,
                        AnswerRequestDto::userAnswer,
                        (oldValue, newValue) -> newValue
                ));

        // 6. 이미 저장된 답안들을 <문제Id, 저장된 답안 객체>로 변환
        Map<Long, AnswerSubmission> existingAnswers = submission.getAnswers().stream()
                .collect(Collectors.toMap(ans -> ans.getQuestion().getId(), ans -> ans));

        // 7. 답안 업데이트 또는 새로 생성
        for (Map.Entry<Long, String> entry : userAnswerMap.entrySet()) {
            Long questionId = entry.getKey();
            String userAnswerText = entry.getValue();

            // 유효한 questionId인지 확인
            if (!questionMap.containsKey(questionId)) {
                log.warn("Attempt to save answer for a non-existent questionId: {}", questionId);
                continue;
            }

            AnswerSubmission answerToUpdate = existingAnswers.get(questionId);
            if (answerToUpdate != null) {
                // 이미 있던 답 -> 내용만 갱신
                answerToUpdate.updateAnswer(userAnswerText);
            } else {
                // 없던 답 -> 새로 생성
                QuizQuestion question = questionMap.get(questionId);
                AnswerSubmission newAnswer = AnswerSubmission.builder()
                        .submission(submission)
                        .question(question)
                        .submittedAnswer(userAnswerText)
                        .isCorrect(false)
                        .build();
                submission.addAnswerSubmission(newAnswer);
            }
        }
    }

    @Transactional
    public QuizResultDto completeQuiz(Long submissionId, User user, List<AnswerRequestDto> answers) {
        // 1. Submission 조회
        QuizSubmission submission = quizSubmissionRepository.findForGradingById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND));

        // 2. 권한 & 상태 확인
        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        submission.validateIsNotCompleted();
        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(submission.getQuiz().getStudy(), user)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        // 3. 답안 유효성 검증
        validateAnswers(submission.getQuiz(), answers);

        // 4. 점수 계산
        int finalScore = gradeAnswers(submission, answers);

        // 5. 제출 완료 처리
        submission.complete(finalScore);

        // 6. rank point 갱신
        rankPointService.addPointsFromQuiz(studyMember, submission);

        return quizDtoMapper.mapToResultDto(submission);
    }

    @Transactional
    public void deleteQuiz(Long quizId, User user) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 권한 검증
        StudyMember member = studyMemberRepository.findByStudyAndUser(quiz.getStudy(), user)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        if (member.getRole() != StudyMemberRole.LEADER) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS, "삭제 권한이 없습니다.");
        }

        // 해당 퀴즈 점수 차감
        rankPointService.revokePointsForQuiz(quizId);

        // 퀴즈 삭제
        quizRepository.delete(quiz);
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

    private void validateAnswers(Quiz quiz, List<AnswerRequestDto> answers) {
        // <문제 ID, 문제 객체> 매핑
        Map<Long, QuizQuestion> questionMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        // 제출된 답안 수 검증
        if (answers.size() > questionMap.size()) {
            throw new BusinessException(ErrorCode.INVALID_ANSWER_SUBMISSION, "제출된 답안의 수가 퀴즈의 총 문제 수보다 많습니다.");
        }

        // 제출된 각 답안 검증
        for (AnswerRequestDto answerDto : answers) {
            QuizQuestion question = questionMap.get(answerDto.questionId());
            // 존재하지 않는 문제 ID 제출 검증
            if (question == null) {
                throw new BusinessException(ErrorCode.INVALID_ANSWER_SUBMISSION, "퀴즈에 존재하지 않는 문제 ID가 포함되어 있습니다: " + answerDto.questionId());
            }

            if (question instanceof MultipleChoiceQuestion mcq) {
                Long userAnswerChoiceId;
                try {
                    userAnswerChoiceId = Long.parseLong(answerDto.userAnswer());
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.INVALID_ANSWER_SUBMISSION, "객관식 답안은 숫자(선택지 ID) 형식이어야 합니다.");
                }

                boolean isValidChoice = mcq.getChoices().stream()
                        .anyMatch(choice -> choice.getId().equals(userAnswerChoiceId));
                if (!isValidChoice) {
                    throw new BusinessException(ErrorCode.INVALID_ANSWER_SUBMISSION, "문제 " + mcq.getId() + "에 존재하지 않는 선택지 ID가 제출되었습니다: " + userAnswerChoiceId);
                }
            }
        }
    }

    private int gradeAnswers(QuizSubmission submission, List<AnswerRequestDto> answers) {
        // 요청으로 받은 최종 답변 매핑 <문제ID, 사용자 답>
        Map<Long, String> userAnswerMap = answers.stream()
                .collect(Collectors.toMap(
                        AnswerRequestDto::questionId,
                        AnswerRequestDto::userAnswer,
                        (oldValue, newValue) -> newValue
                ));

        return submission.getQuiz().getQuestions().stream()
                .mapToInt(question -> {
                    String userAnswer = userAnswerMap.get(question.getId());

                    boolean isAnswered = StringUtils.hasText(userAnswer);
                    int score = isAnswered ? question.calculateScore(userAnswer) : 0;

                    AnswerSubmission answer = submission.findOrCreateAnswer(question);
                    answer.updateFinalAnswer(userAnswer, score > 0);
                    return score;
                })
                .sum();
    }
}