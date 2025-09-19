package com.pado.domain.dashboard.service;

import com.pado.domain.dashboard.dto.StudyDashboardResponseDto;

public interface DashboardService {
    StudyDashboardResponseDto getStudyDashboard(Long studyId);
}
