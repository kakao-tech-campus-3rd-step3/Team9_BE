package com.pado.domain.material.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최신 학습 자료 응답 DTO")
public record RecentMaterialResponseDto(
        @Schema(description = "자료 ID")
        Long material_id,

        @Schema(description = "자료 이름", example = "1주차 학습 자료")
        String material_title,

        @Schema(description = "작성자 닉네임", example = "박지훈")
        String author_name,

        @Schema(description = "해당 자료에 첨부된 총 파일 개수", example = "2")
        Long file_count,

        @Schema(description = "첨부된 모든 파일 크기 합계 (bytes)", example = "819200")
        Long total_file_size
) {
    @QueryProjection
    public RecentMaterialResponseDto(Long material_id, String material_title, String author_name, Long file_count, Long total_file_size) {
        this.material_id = material_id;
        this.material_title = material_title;
        this.author_name = author_name;
        this.file_count = file_count;
        this.total_file_size = total_file_size != null ? total_file_size : 0L;
    }
}