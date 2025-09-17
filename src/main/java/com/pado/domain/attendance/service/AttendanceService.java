package com.pado.domain.attendance.service;


import com.pado.domain.attendance.dto.AttendanceListResponseDto;
import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.user.entity.User;

public interface AttendanceService {
    // 전체 참여 현황 조회
    AttendanceListResponseDto getFullAttendance(Long studyId);
    // 개별 참여 현황 조회
    AttendanceStatusResponseDto getIndividualAttendanceStatus(Long studyId, Long scheduleId,  User user);
    // 출석
    AttendanceStatusResponseDto checkIn(Long ScheduleId, User user);
}
