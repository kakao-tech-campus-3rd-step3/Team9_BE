package com.pado.domain.study.controller;

import com.pado.domain.study.dto.response.MyRankResponseDto;
import com.pado.domain.study.dto.response.StudyRankingResponseDto;
import com.pado.domain.study.service.StudyRankingService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "06. Study Ranking", description = "스터디 랭킹 관련 API")
@RestController
@RequestMapping("/api/studies/{study_id}")
@RequiredArgsConstructor
public class StudyRankingController {

    private final StudyRankingService studyRankingService;

    @GetMapping("/me")
    @Operation(summary = "내 랭킹 정보 조회", description = "스터디 내 현재 사용자의 랭킹과 점수를 조회합니다.")
    @Parameters({
            @Parameter(name = "study_id", description = "조회할 스터디의 ID", required = true, example = "1")
    })
    public ResponseEntity<MyRankResponseDto> getMyRank(
            @PathVariable("study_id") Long studyId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        MyRankResponseDto myRank = studyRankingService.getMyRank(studyId, user.getId());
        return ResponseEntity.ok(myRank);
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "랭킹 조회",
            description = "스터디 내 스터디원들의 랭킹을 조회합니다. (Rank Point 기준 내림차순 정렬)"
    )
    @ApiResponse(
            responseCode = "200", description = "랭킹 조회 성공",
            content = @Content(schema = @Schema(implementation = StudyRankingResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "study_id", description = "랭킹을 조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/ranking")
    public ResponseEntity<StudyRankingResponseDto> getStudyRanking(
            @PathVariable("study_id") Long studyId
    ) {
        // TODO: 랭킹 조회 로직 구현
        List<String> mockRankings = Arrays.asList("파도타기", "랭커유저", "공부왕", "새내기");
        StudyRankingResponseDto mockResponse = new StudyRankingResponseDto(mockRankings);

        return ResponseEntity.ok(mockResponse);
    }
}
