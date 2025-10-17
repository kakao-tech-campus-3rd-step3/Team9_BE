package com.pado.domain.quiz.service;

import com.pado.domain.quiz.entity.QuizPointLog;
import com.pado.domain.quiz.entity.QuizSubmission;
import com.pado.domain.quiz.repository.QuizPointLogRepository;
import com.pado.domain.study.entity.StudyMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankPointService {

    private final QuizPointLogRepository quizPointLogRepository;

    @Transactional
    public void addPointsFromQuiz(StudyMember member, QuizSubmission submission) {
        int score = submission.getScore();
        if (score <= 0) {
            return;
        }

        member.addRankPoints(score);

        QuizPointLog log = QuizPointLog.builder()
                .studyMember(member)
                .quizSubmission(submission)
                .pointsAwarded(score)
                .build();

        quizPointLogRepository.save(log);
    }

    @Transactional
    public void revokePointsForQuiz(Long quizId) {
        // 1. 아직 회수되지 않은 퀴즈 포인트 로그 조회
        List<QuizPointLog> logsToRevoke = quizPointLogRepository.findActiveLogsByQuizIdWithMember(quizId);

        for (QuizPointLog log : logsToRevoke) {
            // 2. 멤버 점수 차감 & 로그 상태 변경
            StudyMember member = log.getStudyMember();
            member.subtractRankPoints(log.getPointsAwarded());
            log.revoke();
        }
    }
}
