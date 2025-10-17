package com.pado.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "특정 스터디원 참여 여부 요청 DTO")
public record AttendanceMemberStatusRequestDto (
        @Schema(description = "스터디원 닉네임")
        String nickname,
        @Schema(description = "참석 여부")
        boolean status
){
}
