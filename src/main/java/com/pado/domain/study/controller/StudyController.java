package com.pado.domain.study.controller;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.study.dto.request.StudyCreateRequestDto;
import com.pado.domain.study.dto.response.StudyDetailResponseDto;
import com.pado.domain.study.dto.response.StudyListResponseDto;
import com.pado.domain.study.dto.response.StudySimpleResponseDto;
import com.pado.domain.shared.entity.Region;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. Study", description = "스터디 생성, 조회 관련 API")
@RestController
@RequestMapping("/api/studies")
public class StudyController {

    // TODO: 서비스 레이어 종속성 주입

    @SecurityRequirements({})
    @Operation(summary = "스터디 목록 조회", description = "필터링 조건(카테고리, 지역)과 페이지네이션을 통해 스터디 목록을 조회합니다. (무한 스크롤)\n\n" +
            "**[Mock 테스트용 안내]**\n" +
            "- page = 0, 1, 2 → hasNext = true 반환\n" +
            "- page ≥ 3 → hasNext = false 반환 (마지막 페이지)")
    @ApiResponse(
            responseCode = "200", description = "스터디 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = StudyListResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "interests", description = "필터링할 카테고리(관심 분야) 목록. 여러 개 가능.", example = "프로그래밍,취업"),
            @Parameter(name = "location", description = "필터링할 지역", example = "서울"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", required = true, example = "0"),
            @Parameter(name = "size", description = "페이지 당 사이즈 (기본값 10)", example = "10")
    })
    @GetMapping
    public ResponseEntity<StudyListResponseDto> getStudyList(
            @RequestParam(required = false) List<String> interests,
            @RequestParam(required = false) String location,
            @RequestParam int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // TODO: 스터디 목록 조회 로직 구현
        List<StudySimpleResponseDto> mockStudies = List.of(
                new StudySimpleResponseDto(1L, "https://pado-image.com/1", "스프링 스터디", "스프링 기초부터 심화까지"),
                new StudySimpleResponseDto(2L, "https://pado-image.com/2", "JPA 스터디", "JPA 정복하기"),
                new StudySimpleResponseDto(3L, "https://pado-image.com/3", "알고리즘 스터디", "코딩 테스트 완전 정복")
        );
        boolean hasNext = page < 3;
        StudyListResponseDto mockResponse = new StudyListResponseDto(mockStudies, page, size, hasNext);
        return ResponseEntity.ok(mockResponse);
    }

    @SecurityRequirements({})
    @Api404StudyNotFoundError
    @Operation(summary = "스터디 상세 정보 조회", description = "지정된 ID를 가진 스터디의 상세 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200", description = "스터디 상세 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = StudyDetailResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "study_id", description = "조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/{study_id}")
    public ResponseEntity<StudyDetailResponseDto> getStudyDetail(
            @PathVariable("study_id") Long studyId
    ) {
        // TODO: 스터디 상세 정보 조회 로직 구현
        StudyDetailResponseDto mockResponse = new StudyDetailResponseDto(
                "https://pado-image.com/detail/1",
                "스프링 심화 스터디",
                "스프링 핵심 기술과 JPA를 깊이 있게 다룹니다.",
                "스프링의 IoC 컨테이너, AOP, 트랜잭션 관리 등 핵심 원리를 학습하고, JPA를 활용한 데이터베이스 연동 프로젝트를 진행합니다.",
                List.of(Category.PROGRAMMING, Category.EMPLOYMENT),
                Region.SEOUL,
                "매주 토요일 오후 2시 - 4시",
                List.of("Spring에 대한 열정이 있으신 분", "주 1회 오프라인 참여 가능하신 분"),
                5,
                10
        );
        return ResponseEntity.ok(mockResponse);
    }

    @Operation(summary = "스터디 생성", description = "새로운 스터디를 생성 (스터디 이름, 한 줄 소개, 스터디 설명, 카테고리, 제한 인원, 이미지)")
    @ApiResponse(
            responseCode = "201", description = "스터디 생성 성공"
    )
    @PostMapping
    public ResponseEntity<Void> createStudy(@Valid @RequestBody StudyCreateRequestDto request) {
        // TODO: 스터디 생성 로직 구현
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @SecurityRequirements({})
    @Operation(summary = "스터디 검색", description = "검색을 통해 스터디 목록을 조회합니다. (무한 스크롤)\n\n" +
            "**[Mock 테스트용 안내]**\n" +
            "- page = 0, 1, 2 → hasNext = true 반환\n" +
            "- page ≥ 3 → hasNext = false 반환 (마지막 페이지)")
    @ApiResponse(
            responseCode = "200", description = "스터디 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = StudyListResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "keyword", description = "검색할 키워드", required = true, example = "스프링"),
            @Parameter(name = "interests", description = "필터링할 카테고리(관심 분야) 목록. 여러 개 가능.", example = "프로그래밍,취업"),
            @Parameter(name = "location", description = "필터링할 지역", example = "서울"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", required = true, example = "0"),
            @Parameter(name = "size", description = "페이지 당 사이즈 (기본값 10)", example = "10")
    })
    @GetMapping("/search")
    public ResponseEntity<StudyListResponseDto> searchStudies(
            @RequestParam String keyword,
            @RequestParam(required = false) List<String> interests,
            @RequestParam(required = false) String location,
            @RequestParam int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // TODO: 스터디 검색 로직 구현
        List<StudySimpleResponseDto> mockStudies = List.of(
                new StudySimpleResponseDto(1L, "https://pado-image.com/1", "스프링 스터디", "스프링 기초부터 심화까지"),
                new StudySimpleResponseDto(4L, "https://pado-image.com/4", "스프링 부트 입문", "최신 스프링 부트로 웹 개발 시작하기"),
                new StudySimpleResponseDto(5L, "https://pado-image.com/5", "스프링 시큐리티", "스프링 시큐리티로 인증/인가 구현")
        );
        boolean hasNext = page < 3;
        StudyListResponseDto mockResponse = new StudyListResponseDto(mockStudies, page, size, hasNext);
        return ResponseEntity.ok(mockResponse);
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "스터디 정보 수정",
            description = "지정된 스터디의 정보를 수정합니다. (스터디 리더만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "스터디 정보 수정 성공"
    )
    @Parameters({
            @Parameter(name = "study_id", description = "수정할 스터디의 ID", required = true, example = "1")
    })
    @PatchMapping("/{study_id}")
    public ResponseEntity<Void> updateStudy(
            @PathVariable("study_id") Long studyId,
            @Valid @RequestBody StudyCreateRequestDto request
    ) {
        // TODO: 스터디 정보 수정 기능 구현
        return ResponseEntity.ok().build();
    }
}

