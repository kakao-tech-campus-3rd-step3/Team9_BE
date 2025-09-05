package com.pado.domain.material.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "학습 자료 생성 요청 DTO")
public record MaterialRequestDto(
        @Schema(description = "자료 제목", example = "스프링 웹 강의 자료")
        @NotBlank(message = "자료 제목은 필수 입력 항목입니다.")
        String title,

        @Schema(description = "자료 카테고리", example = "강의")
        @NotBlank(message = "자료 카테고리는 필수 입력 항목입니다.")
        String category,

        @Schema(description = "자료 내용", example = "스프링 웹 개발의 기본 개념을 정리한 강의 자료입니다.")
        @NotBlank(message = "자료 내용은 필수 입력 항목입니다.")
        String content,

        @Schema(description = "업로드 완료된 첨부파일 정보 목록 (필수 X), 파일 첨부 안하면 files : []")
        List<FileRequestDto> files
) {}
