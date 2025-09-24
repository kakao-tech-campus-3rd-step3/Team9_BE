package com.pado.domain.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 로드맵 차시 추가 요청 DTO")
public record ProgressChapterRequestDto(
        @Schema(description = "차시 별 로드맵 내용", example = "1차시에 진행할 내용입니다")
        String content
){
}
