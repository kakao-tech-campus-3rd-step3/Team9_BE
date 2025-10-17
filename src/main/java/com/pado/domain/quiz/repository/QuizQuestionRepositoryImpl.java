package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizQuestion;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;

import static com.pado.domain.quiz.entity.QQuizQuestion.quizQuestion;

@RequiredArgsConstructor
public class QuizQuestionRepositoryImpl implements QuizQuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<QuizQuestion> findByQuizId(Long quizId) {
        return queryFactory
                .selectFrom(quizQuestion)
                .where(quizQuestion.quiz.id.eq(quizId))
                .fetch();
    }
}