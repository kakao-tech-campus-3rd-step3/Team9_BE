package com.pado.domain.progress.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 로드맵 전체 응답 DTO")
public record ProgressRoadMapResponseDto (
        @Schema(description = "차시별 로드맵 목록")
        List<ProgressChapterDto> chapters
){
}
