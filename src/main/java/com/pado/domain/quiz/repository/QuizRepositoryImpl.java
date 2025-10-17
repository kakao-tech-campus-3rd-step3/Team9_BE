package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.QQuestionCountDto;
import com.pado.domain.quiz.dto.projection.QQuizInfoProjection;
import com.pado.domain.quiz.dto.projection.QuestionCountDto;
import com.pado.domain.quiz.dto.projection.QuizInfoProjection;
import com.pado.domain.quiz.entity.MultipleChoiceQuestion;
import com.pado.domain.quiz.entity.Quiz;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.pado.domain.quiz.entity.QMultipleChoiceQuestion.multipleChoiceQuestion;
import static com.pado.domain.quiz.entity.QQuiz.quiz;
import static com.pado.domain.quiz.entity.QQuizQuestion.quizQuestion;
import static com.pado.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
    public Optional<Quiz> findDetailById(Long quizId) {
        Quiz result = queryFactory
                .selectFrom(quiz)
                .leftJoin(quiz.questions, quizQuestion).fetchJoin()
                .where(quiz.id.eq(quizId))
                .fetchOne();

        if (result == null) {
            return Optional.empty();
        }

        List<MultipleChoiceQuestion> mcqs = result.getQuestions().stream()
                .filter(MultipleChoiceQuestion.class::isInstance)
                .map(MultipleChoiceQuestion.class::cast)
                .toList();

        if (!mcqs.isEmpty()) {
            queryFactory.selectFrom(multipleChoiceQuestion)
                    .join(multipleChoiceQuestion.choices).fetchJoin()
                    .where(multipleChoiceQuestion.in(mcqs))
                    .fetch();
        }

        return Optional.of(result);
    }

    private BooleanExpression cursorIdLessThan(Long cursorId) {
        return cursorId != null
                        ? quiz.id.lt(cursorId)
                        : null;
    }
}