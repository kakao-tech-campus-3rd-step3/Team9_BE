package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.QSubmissionStatusDto;
import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.entity.*;
import com.pado.domain.quiz.repository.dto.UserQuizCountDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.pado.domain.quiz.entity.QAnswerSubmission.answerSubmission;
import static com.pado.domain.quiz.entity.QQuiz.quiz;
import static com.pado.domain.quiz.entity.QQuizSubmission.quizSubmission;

@RequiredArgsConstructor
public class QuizSubmissionRepositoryImpl implements QuizSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QQuizQuestion questionFromAnswer = new QQuizQuestion("questionFromAnswer");

    @Override
    public List<SubmissionStatusDto> findSubmissionStatuses(List<Long> quizIds, Long userId) {
        if (quizIds == null || quizIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .select(new QSubmissionStatusDto(
                        quizSubmission.quiz.id,
                        quizSubmission.id,
                        quizSubmission.status,
                        quizSubmission.score
                ))
                .from(quizSubmission)
                .where(
                        quizSubmission.quiz.id.in(quizIds),
                        quizSubmission.user.id.eq(userId)
                )
                .fetch();
    }

    @Override
    public Optional<QuizSubmission> findForInProgressById(Long submissionId) {
        QuizSubmission submission = queryFactory
                .selectFrom(quizSubmission)
                .join(quizSubmission.quiz, quiz).fetchJoin()
                .leftJoin(quizSubmission.answers, answerSubmission).fetchJoin()
                .leftJoin(answerSubmission.question, questionFromAnswer).fetchJoin()
                .where(quizSubmission.id.eq(submissionId))
                .fetchOne();

        return Optional.ofNullable(submission);
    }

    @Override
    public Optional<QuizSubmission> findForGradingById(Long submissionId) {
        QuizSubmission submission = queryFactory
                .selectFrom(QQuizSubmission.quizSubmission)
                .join(QQuizSubmission.quizSubmission.quiz, quiz).fetchJoin()
                .where(QQuizSubmission.quizSubmission.id.eq(submissionId))
                .fetchOne();

        if (submission == null) {
            return Optional.empty();
        }

        List<Long> mcqIds = submission.getQuiz().getQuestions().stream()
                .filter(q -> q instanceof MultipleChoiceQuestion)
                .map(QuizQuestion::getId)
                .toList();

        if (!mcqIds.isEmpty()) {
            queryFactory
                    .selectFrom(QMultipleChoiceQuestion.multipleChoiceQuestion)
                    .leftJoin(QMultipleChoiceQuestion.multipleChoiceQuestion.choices).fetchJoin()
                    .leftJoin(QMultipleChoiceQuestion.multipleChoiceQuestion.correctChoice).fetchJoin()
                    .where(QMultipleChoiceQuestion.multipleChoiceQuestion.id.in(mcqIds))
                    .fetch();
        }

        return Optional.of(submission);
    }

    @Override
    public List<UserQuizCountDto> countByStudyGroupByUser(Long studyId) {
        QQuizSubmission qs = QQuizSubmission.quizSubmission;

        return queryFactory
                .select(Projections.constructor(UserQuizCountDto.class,
                        qs.user.id,
                        qs.count())) // count(qs)
                .from(qs)
                .where(qs.quiz.study.id.eq(studyId))
                .groupBy(qs.user.id)
                .fetch();
    }
}