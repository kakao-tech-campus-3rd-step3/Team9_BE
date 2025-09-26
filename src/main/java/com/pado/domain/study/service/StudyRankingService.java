package com.pado.domain.study.service;

import com.pado.domain.quiz.entity.QuizSubmission;
import com.pado.domain.study.dto.response.MyRankResponseDto;
import com.pado.domain.study.dto.response.TotalRankingResponseDto;
import com.pado.domain.study.entity.StudyMember;

public interface StudyRankingService {
    MyRankResponseDto getMyRank(Long studyId, Long userId);
    TotalRankingResponseDto getTotalRanking(Long studyId);
    void addPointsFromQuiz(StudyMember member, QuizSubmission submission);
    void revokePointsForQuiz(Long quizId);
}
