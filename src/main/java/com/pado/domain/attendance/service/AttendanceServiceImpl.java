package com.pado.domain.attendance.service;

import com.pado.domain.attendance.dto.AttendanceListResponseDto;
import com.pado.domain.attendance.dto.AttendanceStatusDto;
import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.attendance.dto.MemberAttendanceDto;
import com.pado.domain.attendance.entity.Attendance;
import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyRepository studyRepository;

    @Override
    @Transactional(readOnly = true)
    public AttendanceListResponseDto getFullAttendance(Long studyId) {
        Study study = studyRepository.findById(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        Long leaderId = studyMemberRepository.findLeaderUserIdByStudy(study,
            StudyMemberRole.LEADER);
        List<StudyMember> studyMembers = studyMemberRepository.findByStudyWithUser(study);
        List<Schedule> schedules = scheduleRepository.findByStudyIdOrderByStartTimeAsc(studyId);
        List<Attendance> attendances = attendanceRepository.findAllByStudyIdWithScheduleAndUser(
            studyId);
        List<StudyMember> orderedMembers = studyMembers.stream()
            .sorted(Comparator.comparing(sm -> !sm.getUser().getId().equals(leaderId)))
            .toList();

        Map<String, Attendance> attendanceMap = attendances.stream()
            .collect(Collectors.toMap(
                attendance -> attendance.getUser().getId() + "-" + attendance.getSchedule().getId(),
                attendance -> attendance
            ));

        // MemberAttendanceDto List 생성
        List<MemberAttendanceDto> memberAttendanceDtoList = orderedMembers.stream()
            .map(studyMember -> {
                User user = studyMember.getUser();

                //AttendanceStatusDto List 생성
                List<AttendanceStatusDto> attendanceStatusDtoList = schedules.stream()
                    .map(schedule -> {
                        Attendance attendance = attendanceMap.get(
                            user.getId() + "-" + schedule.getId());
                        boolean status = (attendance != null) && (attendance.isStatus());
                        return new AttendanceStatusDto(status, schedule.getStartTime());
                    })
                    .toList();
                return new MemberAttendanceDto(user.getNickname(), user.getProfileImageUrl(),
                    attendanceStatusDtoList);
            })
            .toList();

        return new AttendanceListResponseDto(memberAttendanceDtoList);
    }


    @Override
    public AttendanceStatusResponseDto getIndividualAttendanceStatus(Long scheduleId, User user) {
        Schedule schedule = checkException(scheduleId, user);
        return new AttendanceStatusResponseDto(
            attendanceRepository.existsByScheduleAndUser(schedule, user));
    }


    @Override
    public AttendanceStatusResponseDto checkIn(Long scheduleId, User user) {
        Schedule schedule = checkException(scheduleId, user);

        // 출석 여부 확인
        if (attendanceRepository.existsByScheduleAndUser(schedule, user)) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
        }

        Attendance attendance = Attendance.createCheckIn(schedule, user);
        attendanceRepository.save(attendance);

        return new AttendanceStatusResponseDto(true);
    }

    // 공통 예외 검증
    private Schedule checkException(Long scheduleId, User user) {
        // 스케줄 검증
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 스터디 검증
        Study study = studyRepository.findById(schedule.getStudyId())
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        // 스터디 멤버 검증
        if (!studyMemberRepository.existsByStudyAndUser(study, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        return schedule;
    }

}
