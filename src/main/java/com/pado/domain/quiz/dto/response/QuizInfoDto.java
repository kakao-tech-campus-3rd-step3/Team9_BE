package com.pado.domain.quiz.dto.response;

import com.pado.domain.quiz.dto.projection.QuizInfoProjection;
import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.entity.QuizStatus;

public record QuizInfoDto(
        Long quizId,
        String title,
        String createdBy,
        int questionCount,
        Integer timeLimitSeconds,
        QuizStatus quizStatus,
        String userSubmissionStatus
)  implements CursorIdentifiable {

    @Override
    public Long getCursorId() {
        return this.quizId;
    }

    public static QuizInfoDto of(QuizInfoProjection proj, int questionCount, SubmissionStatusDto submissionInfo) {
        return new QuizInfoDto(
                proj.quizId(),
                proj.title(),
                proj.createdBy(),
                questionCount,
                proj.timeLimitSeconds(),
                proj.quizStatus(),
                formatUserStatus(submissionInfo, questionCount)
        );
    }

    private static String formatUserStatus(SubmissionStatusDto submission, int totalQuestions) {
        if (submission == null) {
            return "미응시";
        }

        return switch (submission.status()) {
            case IN_PROGRESS -> "진행중";
            case COMPLETED -> String.format("%d/%d개 정답", submission.score(), totalQuestions);
        };
    }
}