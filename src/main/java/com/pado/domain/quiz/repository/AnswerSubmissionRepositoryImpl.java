package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.AnswerSubmission;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;

import static com.pado.domain.quiz.entity.QAnswerSubmission.answerSubmission;
import static com.pado.domain.quiz.entity.QQuizQuestion.quizQuestion;

@RequiredArgsConstructor
public class AnswerSubmissionRepositoryImpl implements AnswerSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AnswerSubmission> findWithQuestionBySubmissionId(Long submissionId) {
        return queryFactory
                .selectFrom(answerSubmission)
                .join(answerSubmission.question, quizQuestion).fetchJoin()
                .where(answerSubmission.submission.id.eq(submissionId))
                .fetch();
    }
}