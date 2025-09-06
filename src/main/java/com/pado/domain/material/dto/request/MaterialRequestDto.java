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

        @Schema(description = """
                업로드할 파일 정보 목록 (선택사항)
                - 자료 생성 시: id는 모두 null
                - 자료 수정 시: 
                  * 기존 파일 유지: id 포함
                  * 새 파일 추가: id는 null  
                  * 파일 삭제: 해당 파일을 목록에서 제외
                  * 모든 파일 삭제: 빈 배열 []
                  * 파일 변경 없음: null
                """,
                example = """
                [
                  {"id": 1, "name": "기존파일.pdf", "url": "https://..."},
                  {"id": null, "name": "새파일.jpg", "url": "https://..."}
                ]
                """)
        List<FileRequestDto> files
) {}
