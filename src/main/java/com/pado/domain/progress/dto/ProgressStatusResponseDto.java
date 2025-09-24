package com.pado.domain.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디원 진척도 전체 응답 DTO")
public record ProgressStatusResponseDto (
        @Schema(description = "스터디원 별 진척도 목록")
        List<ProgressMemberStatusDto> progressMemberStatusDto
){
}
