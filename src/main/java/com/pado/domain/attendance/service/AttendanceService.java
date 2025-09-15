package com.pado.domain.attendance.service;


import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.user.entity.User;

public interface AttendanceService {
    AttendanceStatusResponseDto checkIn(Long ScheduleId, User user);
}
