package com.pado.domain.study.controller;

import com.pado.domain.study.dto.request.StudyApplicationStatusChangeRequestDto;
import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.study.dto.request.StudyMemberRoleChangeRequestDto;
import com.pado.domain.study.dto.response.StudyMemberDetailDto;
import com.pado.domain.study.dto.response.StudyMemberListResponseDto;
import com.pado.domain.study.dto.response.UserDetailDto;
import com.pado.domain.study.service.StudyMemberService;
import com.pado.domain.study.service.StudyService;
import com.pado.domain.user.entity.Gender;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.exception.dto.ErrorResponseDto;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import com.pado.global.swagger.annotation.study.Api404StudyOrMemberNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "05. Study Member", description = "스터디원 관리 및 신청 관련 API")
@RestController
@RequestMapping("/api/studies/{study_id}")
@RequiredArgsConstructor
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    @Api404StudyNotFoundError
    @Operation(summary = "스터디 참여 신청", description = "사용자가 스터디에 참여 신청을 보냅니다.")
    @ApiResponse(
            responseCode = "201", description = "스터디 참여 신청 성공"
    )
    @Parameters({
            @Parameter(name = "study_id", description = "참여 신청할 스터디의 ID", required = true, example = "1")
    })
    @PostMapping("/apply")
    public ResponseEntity<Void> applyToStudy(
            @Parameter(hidden = true) @CurrentUser User user,
            @PathVariable("study_id") Long studyId,
            @Valid @RequestBody StudyApplyRequestDto request
    ) {
        studyMemberService.applyToStudy(user, studyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "스터디원 목록 조회",
            description = "스터디원 목록을 조회합니다. (스터디 리더만 접근 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "스터디원 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = StudyMemberListResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "study_id", description = "조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/member")
    public ResponseEntity<StudyMemberListResponseDto> getStudyMembers(
            @PathVariable("study_id") Long studyId
    ) {
        // TODO: 스터디원 목록 조회 로직 구현
        List<StudyMemberDetailDto> members = List.of(
                new StudyMemberDetailDto("리더유저", "Leader", null, new UserDetailDto("https://pado-image.com/user/1", "Women", List.of("프로그래밍", "취업"), "서울")),
                new StudyMemberDetailDto("참여자1", "Member", null, new UserDetailDto("https://pado-image.com/user/2", "Men", List.of("어학", "고시/공무원"), "경기")),
                new StudyMemberDetailDto("신청자1", "Pending", "안녕하세요. 열심히 참여하고 싶습니다!", new UserDetailDto("https://pado-image.com/user/3", "Men", List.of("취미/교양"), "부산"))
        );
        return ResponseEntity.ok(new StudyMemberListResponseDto(members));
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyOrMemberNotFoundError
    @Operation(
            summary = "특정 스터디원 탈퇴/신청 거부",
            description = "특정 스터디원을 탈퇴시키거나, 스터디 신청을 거부합니다. (스터디 리더만 가능)"
    )
    @ApiResponse(
            responseCode = "204", description = "탈퇴/거부 성공"
    )
    @Parameters({
            @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1"),
            @Parameter(name = "member_id", description = "탈퇴시키거나 거부할 스터디원의 ID", required = true, example = "2")
    })
    @DeleteMapping("/member/{member_id}")
    public ResponseEntity<Void> kickMember(
            @PathVariable("study_id") Long studyId,
            @PathVariable("member_id") Long memberId
    ) {
        // TODO: 특정 스터디원 탈퇴/신청 거부 로직 구현
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyOrMemberNotFoundError
    @Operation(
            summary = "스터디원 상태 변경",
            description = "특정 스터디원의 역할을 변경합니다. (스터디 리더만 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경/수락 성공"),
            @ApiResponse(responseCode = "409", description = "유효하지 않은 상태 변경")
    })
    @Parameters({
            @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1"),
            @Parameter(name = "member_id", description = "역할을 변경할 스터디원의 ID", required = true, example = "2")
    })
    @PatchMapping("/member/{member_id}")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable("study_id") Long studyId,
            @PathVariable("member_id") Long memberId,
            @Valid @RequestBody StudyMemberRoleChangeRequestDto request
    ) {
        // TODO: 스터디원 상태 변경
        return ResponseEntity.ok().build();
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyOrMemberNotFoundError
    @Operation(
            summary = "스터디원 상태 변경",
            description = "특정 스터디원의 역할을 변경합니다. (스터디 리더만 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경/수락 성공"),
            @ApiResponse(responseCode = "409", description = "유효하지 않은 상태 변경")
    })
    @Parameters({
            @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1"),
            @Parameter(name = "member_id", description = "역할을 변경할 스터디원의 ID", required = true, example = "2")
    })
    @PatchMapping("/applications/{application_id}")
    public ResponseEntity<Void> updateMemberApplicationStatus(
            @Parameter(hidden = true) @CurrentUser User user,
            @PathVariable("study_id") Long studyId,
            @PathVariable("application_id") Long applicationId,
            @Valid @RequestBody StudyApplicationStatusChangeRequestDto request
    ) {
        studyMemberService.updateApplicationStatus(user, studyId, applicationId, request);
        return ResponseEntity.ok().build();
    }
}