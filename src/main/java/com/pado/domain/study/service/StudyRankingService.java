package com.pado.domain.study.service;

import com.pado.domain.study.dto.response.MyRankResponseDto;
import com.pado.domain.study.dto.response.TotalRankingResponseDto;
import com.pado.domain.user.entity.User;

public interface StudyRankingService {
    MyRankResponseDto getMyRank(Long studyId, User user);
    TotalRankingResponseDto getTotalRanking(Long studyId, User user);
}
