package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.*;
import com.pado.domain.quiz.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.pado.domain.quiz.entity.QQuiz.quiz;
import static com.pado.domain.quiz.entity.QQuizQuestion.quizQuestion;
import static com.pado.domain.quiz.entity.QQuizSubmission.quizSubmission;
import static com.pado.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Quiz> findWithSourceFilesById(Long id) {
        Quiz result = queryFactory
                .selectFrom(quiz)
                .join(quiz.sourceFiles).fetchJoin()
                .where(quiz.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<QuizInfoProjection> findByStudyIdWithCursor(Long studyId, Long cursor, int pageSize) {
        return queryFactory
                .select(new QQuizInfoProjection(
                        quiz.id,
                        quiz.title,
                        quiz.createdBy.nickname,
                        quiz.timeLimitSeconds,
                        quiz.status
                ))
                .from(quiz)
                .join(quiz.createdBy, user)
                .where(
                        quiz.study.id.eq(studyId),
                        cursorIdLessThan(cursor)
                )
                .orderBy(quiz.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Map<Long, Long> findQuestionCountsByQuizIds(List<Long> quizIds) {
        if (quizIds == null || quizIds.isEmpty()) {
            return Map.of();
        }

        return queryFactory
                .select(new QQuestionCountDto(
                        quizQuestion.quiz.id,
                        quizQuestion.id.count()
                ))
                .from(quizQuestion)
                .where(quizQuestion.quiz.id.in(quizIds))
                .groupBy(quizQuestion.quiz.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        QuestionCountDto::quizId,
                        QuestionCountDto::questionCount
                ));
    }

    @Override
    public Optional<Quiz> findForStartQuizById(Long quizId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(quiz)
                        .leftJoin(quiz.submissions, quizSubmission).fetchJoin()
                        .leftJoin(quizSubmission.user, user).fetchJoin()
                        .where(quiz.id.eq(quizId))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

    @Override
    public List<DashboardQuizProjection> findRecentDashboardQuizzes(Long studyId, Long userId, int size) {
        return queryFactory
                .select(new QDashboardQuizProjection(
                        quiz.id,
                        quiz.title,
                        quizSubmission.status.stringValue()
                ))
                .from(quiz)
                .leftJoin(quizSubmission).on(
                        quizSubmission.quiz.id.eq(quiz.id)
                        .and(quizSubmission.user.id.eq(userId))
                )
                .where(
                        quiz.study.id.eq(studyId),
                        quiz.status.eq(QuizStatus.ACTIVE)
                )
                .orderBy(quiz.createdAt.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression cursorIdLessThan(Long cursorId) {
        return cursorId != null
                ? quiz.id.lt(cursorId)
                : null;
    }
}