package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizPointLog;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;

import static com.pado.domain.quiz.entity.QQuizPointLog.quizPointLog;
import static com.pado.domain.study.entity.QStudyMember.studyMember;

@RequiredArgsConstructor
public class QuizPointLogRepositoryImpl implements QuizPointLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<QuizPointLog> findActiveLogsForQuiz(Long quizId) {
        return queryFactory
                .selectFrom(quizPointLog)
                .join(quizPointLog.studyMember, studyMember).fetchJoin()
                .where(
                        quizPointLog.quizSubmission.quiz.id.eq(quizId),
                        quizPointLog.revoked.isFalse()
                )
                .fetch();
    }
}