package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디원 목록 조회 응답 DTO")
public record StudyMemberListResponseDto(
        @Schema(description = "스터디원 정보 리스트")
        List<StudyMemberDetailDto> members
) {}
