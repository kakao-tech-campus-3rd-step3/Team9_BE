package com.pado.domain.material.dto.request;

import com.pado.domain.material.entity.MaterialCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "학습 자료 생성/수정 요청 DTO")
public record MaterialRequestDto(
        @Schema(description = "자료 제목", example = "스프링 웹 강의 자료")
        @NotBlank(message = "자료 제목은 필수 입력 항목입니다.")
        String title,

        @Schema(description = "자료 카테고리", example = "NOTICE", allowableValues = {"NOTICE", "LEARNING", "ASSIGNMENT"})
        @NotNull(message = "자료 카테고리는 필수 입력 항목입니다.")
        MaterialCategory category,

        @Schema(
                description = "주차 정보 (학습자료에만 필수, 다른 카테고리는 null)",
                example = "1",
                nullable = true
        )
        Integer week,

        @Schema(description = "자료 내용", example = "스프링 웹 개발의 기본 개념을 정리한 강의 자료입니다.")
        @NotBlank(message = "자료 내용은 필수 입력 항목입니다.")
        String content,

        @Schema(description = """
                업로드할 파일 정보 목록 (선택사항)
                - 자료 생성 시: 
                  * id는 모두 null
                  * 파일 첨부 x: 빈 배열 []
                - 자료 수정 시: 
                  * 기존 파일 유지: id 포함
                  * 새 파일 추가: id는 null  
                  * 파일 삭제: 해당 파일을 목록에서 제외
                  * 모든 파일 삭제: 빈 배열 []
                """,
                example = """
                [
                  {"id": 1, "name": "기존파일.pdf", "url": "https://..."},
                  {"id": null, "name": "새파일.jpg", "url": "https://..."}
                ]
                """)
        @NotNull(message = "files는 null일 수 없습니다.")
        List<FileRequestDto> files
) {}
