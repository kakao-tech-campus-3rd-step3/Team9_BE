package com.pado.domain.study.service;

import com.pado.domain.study.dto.response.MyRankResponseDto;

public interface StudyRankingService {
    MyRankResponseDto getMyRank(Long studyId, Long userId);
}
