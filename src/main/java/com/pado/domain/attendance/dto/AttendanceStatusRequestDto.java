package com.pado.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AttendanceStatusRequestDto(
        @Schema(description = "참석 여부")
        boolean status
) {
}
