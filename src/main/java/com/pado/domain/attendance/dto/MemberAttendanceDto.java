package com.pado.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디원 참여 현황 DTO")
public record MemberAttendanceDto(
        @Schema(description = "회원 닉네임", example = "파도타기")
        String name,

        @Schema(description = "회원 프로필 이미지 URL", example = "https://pado-image.com/user/1")
        String image_key,

        @Schema(description = "출석 정보 목록")
        List<AttendanceStatusDto> attendance
) {}