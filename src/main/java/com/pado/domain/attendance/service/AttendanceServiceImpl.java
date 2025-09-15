package com.pado.domain.attendance.service;

import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.attendance.entity.Attendance;
import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyRepository studyRepository;

    @Override
    public AttendanceStatusResponseDto checkIn(Long scheduleId, User user) {
        //스케줄 검증
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 스터디 검증
        Study study = studyRepository.findById(schedule.getStudyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        // 스터디 멤버 검증
        if (!studyMemberRepository.existsByStudyAndUser(study, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        // 출석 여부 확인
        if(attendanceRepository.existsByScheduleAndUser(schedule, user)) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
        }

        Attendance attendance = Attendance.createCheckIn(schedule, user);
        attendanceRepository.save(attendance);

        return new AttendanceStatusResponseDto(true);
    }
}
