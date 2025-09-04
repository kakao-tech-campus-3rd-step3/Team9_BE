package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 목록 조회 응답 DTO")
public record StudyListResponseDto(
        @Schema(description = "스터디 정보 리스트")
        List<StudySimpleResponseDto> studies,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 당 스터디 수", example = "10")
        int size,

        @Schema(description = "다음 페이지 존재 여부 (무한 스크롤)", example = "true")
        boolean has_next
) {}