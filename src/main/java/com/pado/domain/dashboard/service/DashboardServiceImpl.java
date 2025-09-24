package com.pado.domain.dashboard.service;

import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.dashboard.dto.LatestNoticeDto;
import com.pado.domain.dashboard.dto.StudyDashboardResponseDto;
import com.pado.domain.dashboard.dto.UpcomingScheduleDto;
import com.pado.domain.material.entity.MaterialCategory;
import com.pado.domain.material.repository.MaterialRepository;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final StudyRepository studyRepository;
    private final MaterialRepository materialRepository;
    private final ScheduleRepository scheduleRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final AttendanceRepository attendanceRepository;
    private final Clock clock;

    public StudyDashboardResponseDto getStudyDashboard(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);

        LatestNoticeDto latestNotice = findLatestNotice(studyId);
        UpcomingScheduleDto upcomingSchedule = findUpcomingSchedule(studyId, now);

        return new StudyDashboardResponseDto(study.getTitle(), latestNotice, upcomingSchedule);
    }

    private LatestNoticeDto findLatestNotice(Long studyId) {
        return materialRepository.findRecentNoticeAsDto(studyId, MaterialCategory.NOTICE)
                .orElse(null);
    }

    private UpcomingScheduleDto findUpcomingSchedule(Long studyId, LocalDateTime now) {
        int totalMemberCount = studyMemberRepository.countByStudyId(studyId);

        return scheduleRepository.findTopByStudyIdAndStartTimeAfterOrderByStartTimeAsc(studyId, now)
                .map(schedule -> {
                    long dDay = ChronoUnit.DAYS.between(now.toLocalDate(), schedule.getStartTime().toLocalDate());
                    int participantCount = attendanceRepository.countByScheduleId(schedule.getId());
                    return new UpcomingScheduleDto(
                            schedule.getId(),
                            schedule.getTitle(),
                            schedule.getStartTime(),
                            dDay,
                            participantCount,
                            totalMemberCount
                    );
                })
                .orElse(null);
    }
}