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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public StudyDashboardResponseDto getStudyDashboard(Long studyId) {
        return getStudyDashboard(studyId, LocalDateTime.now());
    }

    private StudyDashboardResponseDto getStudyDashboard(Long studyId, LocalDateTime now) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new EntityNotFoundException("스터디를 찾을 수 없습니다. ID: " + studyId));

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