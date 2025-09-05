package com.pado.domain.material.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "학습 자료 생성 요청 DTO")
public record MaterialCreateRequestDto(
        @Schema(description = "자료 제목", example = "스프링 웹 강의 자료")
        @NotBlank(message = "자료 제목은 필수 입력 항목입니다.")
        String title,

        @Schema(description = "자료 카테고리", example = "강의")
        @NotBlank(message = "자료 카테고리는 필수 입력 항목입니다.")
        String category,

        @Schema(description = "자료 내용", example = "스프링 웹 개발의 기본 개념을 정리한 강의 자료입니다.")
        @NotBlank(message = "자료 내용은 필수 입력 항목입니다.")
        String content

        // multipart/form-data를 통해 자료 정보와 파일 자체를 하나의 요청에 담아 한번에 보낼 예정
//        @Schema(description = "자료 URL 리스트", example = "[\"https://pado-storage.com/data1.pdf\", \"https://pado-storage.com/data2.zip\"]")
//        @NotEmpty(message = "자료 URL은 최소 한 개 이상 입력해야 합니다.")
//        List<String> dataUrls
) {}
