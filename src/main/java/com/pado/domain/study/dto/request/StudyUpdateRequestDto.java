package com.pado.domain.study.dto.request;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "스터디 수정 요청 DTO (PATCH용)")
public record StudyUpdateRequestDto(

    @Schema(description = "스터디 제목", example = "스프링 스터디")
    @Size(min = 1, max = 50, message = "스터디 제목은 1자 이상 50자 이하로 입력해주세요.")
    String title,

    @Schema(description = "스터디 한 줄 소개", example = "스프링 기초부터 심화까지")
    @Size(min = 1, max = 100, message = "한 줄 소개는 1자 이상 100자 이하로 입력해주세요.")
    String description,

    @Schema(description = "스터디 상세 설명", example = "스프링의 핵심 원리부터 웹 애플리케이션 개발까지 깊이 있게 다룹니다.")
    String detail_description,

    @Schema(description = "스터디 관심 분야(카테고리) 목록", example = "[\"프로그래밍\", \"취업\"]")
    @Size(min = 1, message = "관심 분야는 최소 1개 이상 선택해야 합니다.")
    List<Category> interests,

    @Schema(description = "스터디 지역", example = "대구")
    Region region,

    @Schema(description = "스터디 시간", example = "매주 토요일 14:00~16:00")
    String study_time,

    @Schema(description = "최대 스터디원 수", example = "10")
    @Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
    Integer max_members,

    @Schema(description = "스터디 참여 조건", example = "[\"해당 분야에 대한 기본적인 관심\", \"정기적인 참여 가능\", \"적극적인 소통과 협력\"]")
    List<String> conditions,

    @Schema(description = "스터디 대표 이미지 파일 키 (S3에 저장된 객체 경로)", example = "study/12345/main.png")
    String file_key
) {

}