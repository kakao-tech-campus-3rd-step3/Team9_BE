package com.pado.domain.attendance.service;


import com.pado.domain.attendance.dto.AttendanceListResponseDto;
import com.pado.domain.attendance.dto.AttendanceMemberStatusRequestDto;
import com.pado.domain.attendance.dto.AttendanceStatusRequestDto;
import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.user.entity.User;

public interface AttendanceService {

    // 전체 참여 현황 조회
    AttendanceListResponseDto getFullAttendance(Long studyId);

    // 개별 참여 현황 조회
    AttendanceStatusResponseDto getMyAttendanceStatus(Long scheduleId, User user);

    //특정 스케줄 맴버 참가 현황 조회
    AttendanceListResponseDto getScheduleAttendance(Long studyId, Long scheduleId, User user);

    //리더가 맴버 참가여부 수정
    void changeMemberAttendanceStatus(Long scheduleId, AttendanceMemberStatusRequestDto attendanceMemberStatusRequestDto, User user);

    // 본인 참가여부 표시
    void changeMyAttendanceStatus(Long ScheduleId, AttendanceStatusRequestDto attendanceStatusRequestDto, User user);


}
