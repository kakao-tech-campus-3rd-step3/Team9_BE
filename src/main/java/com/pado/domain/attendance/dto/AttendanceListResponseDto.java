package com.pado.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "전체 참여 현황 조회 응답 DTO")
public record AttendanceListResponseDto(
        @Schema(description = "스터디원 참여 현황 목록")
        List<MemberAttendanceDto> members
) {}