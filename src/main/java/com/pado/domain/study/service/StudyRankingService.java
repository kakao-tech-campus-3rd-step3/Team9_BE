package com.pado.domain.study.service;

import com.pado.domain.study.dto.response.MyRankResponseDto;
import com.pado.domain.study.dto.response.TotalRankingResponseDto;

public interface StudyRankingService {
    MyRankResponseDto getMyRank(Long studyId, Long userId);
    TotalRankingResponseDto getTotalRanking(Long studyId);
}
