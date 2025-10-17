package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.AnswerSubmission;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;

import static com.pado.domain.quiz.entity.QAnswerSubmission.answerSubmission;

@RequiredArgsConstructor
public class AnswerSubmissionRepositoryImpl implements AnswerSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AnswerSubmission> findBySubmissionId(Long submissionId) {
        return queryFactory
                .selectFrom(answerSubmission)
                .where(answerSubmission.submission.id.eq(submissionId))
                .fetch();
    }
}