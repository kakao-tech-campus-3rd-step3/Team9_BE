package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.QSubmissionStatusDto;
import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.entity.QuizSubmission;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.pado.domain.quiz.entity.QQuiz.quiz;
import static com.pado.domain.quiz.entity.QQuizQuestion.quizQuestion;
import static com.pado.domain.quiz.entity.QQuizSubmission.quizSubmission;

@RequiredArgsConstructor
public class QuizSubmissionRepositoryImpl implements QuizSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SubmissionStatusDto> findSubmissionStatuses(List<Long> quizIds, Long userId) {
        if (quizIds == null || quizIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .select(new QSubmissionStatusDto(
                        quizSubmission.quiz.id,
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
    public Optional<QuizSubmission> findWithDetailsById(Long submissionId) {
        QuizSubmission result = queryFactory
                .selectFrom(quizSubmission)
                .join(quizSubmission.quiz, quiz).fetchJoin()
                .leftJoin(quiz.questions, quizQuestion).fetchJoin()
                .where(quizSubmission.id.eq(submissionId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}