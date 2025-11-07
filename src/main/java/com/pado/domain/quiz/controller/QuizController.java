package com.pado.domain.quiz.controller;

import com.pado.domain.quiz.dto.request.QuizCreateRequestDto;
import com.pado.domain.quiz.dto.request.QuizSubmissionRequestDto;
import com.pado.domain.quiz.dto.response.*;
import com.pado.domain.quiz.service.QuizQueryService;
import com.pado.domain.quiz.service.QuizCommandService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "12. Quiz", description = "퀴즈 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizCommandService quizService;
    private final QuizQueryService quizQueryService;

    @PostMapping("/studies/{study_id}/quizzes")
    @Operation(
            summary = "AI 퀴즈 생성 요청",
            description = "선택된 파일들과 제목으로 AI 퀴즈 생성을 요청합니다. 비동기로 처리됩니다.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "퀴즈 생성 요청 수락"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @Parameter(name = "study_id", description = "스터디 ID", example = "1")
    public ResponseEntity<Void> createQuiz(
            @PathVariable("study_id") Long studyId,
            @Parameter(hidden = true) @CurrentUser User user,
            @Valid @RequestBody QuizCreateRequestDto request
    ) {
        quizService.requestQuizGeneration(user, request.title(), request.fileIds(), studyId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/quizzes/{quiz_id}/regenerate")
    @Operation(summary = "실패한 퀴즈 재생성 요청", description = "생성에 실패한 퀴즈에 대해 재생성을 요청합니다.")
    @Parameter(name = "quiz_id", description = "퀴즈 ID", example = "1")
    public ResponseEntity<Void> requestQuizRegeneration(
            @PathVariable("quiz_id") Long quizId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        quizService.requestQuizRegeneration(quizId, user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/studies/{study_id}/quizzes")
    @Operation(
            summary = "스터디별 퀴즈 목록 조회",
            description = "스터디에 속한 퀴즈 목록을 무한 스크롤 방식으로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "퀴즈 목록 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음")
            }
    )
    @Parameter(name = "study_id", description = "스터디 ID", example = "1")
    public ResponseEntity<CursorResponseDto<QuizInfoDto>> getQuizzes(
            @PathVariable("study_id") Long studyId,
            @Parameter(hidden = true) @CurrentUser User user,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        CursorResponseDto<QuizInfoDto> response = quizQueryService.findQuizzesByStudy(studyId, user, cursor, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/studies/{study_id}/quizzes/recent")
    @Operation(
            summary = "스터디 대시보드용 최신 퀴즈 목록",
            description = "특정 스터디의 최신 퀴즈 목록을 지정된 개수(기본 3개)만큼 조회합니다. (사용자의 응시 상태를 포함합니다)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "퀴즈 목록 조회 성공")
            }
    )
    @Parameter(name = "study_id", description = "스터디 ID", example = "1")
    public ResponseEntity<List<QuizDashboardDto>> getRecentQuizzesForStudyDashboard(
            @PathVariable("study_id") Long studyId,
            @Parameter(hidden = true) @CurrentUser User user,
            @RequestParam(defaultValue = "3") int size
    ) {
        List<QuizDashboardDto> response = quizQueryService.findRecentQuizzesForDashboard(studyId, user, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/quizzes/{quiz_id}")
    @Operation(summary = "퀴즈 풀기 시작", description = "퀴즈 풀기를 시작하고, 진행 중인 답안지가 있으면 그 정보를 반환합니다.")
    @Parameter(name = "quiz_id", description = "퀴즈 ID", example = "1")
    public ResponseEntity<QuizProgressDto> startQuiz(
            @PathVariable("quiz_id") Long quizId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        QuizProgressDto response = quizService.startQuiz(quizId, user);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/submissions/{submission_id}/answers")
    @Operation(summary = "퀴즈 답안 중간 저장", description = "진행 중인 퀴즈의 답안을 중간 저장합니다. 채점은 하지 않습니다.")
    @Parameter(name = "submission_id", description = "답안지 ID", example = "1")
    public ResponseEntity<Void> saveInProgressAnswers(
            @PathVariable("submission_id") Long submissionId,
            @Parameter(hidden = true) @CurrentUser User user,
            @Valid @RequestBody QuizSubmissionRequestDto request
    ) {
        quizService.saveInProgressAnswers(submissionId, user, request.answers());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/submissions/{submission_id}/complete")
    @Operation(summary = "퀴즈 답안 제출", description = "진행 중인 퀴즈의 답안을 최종 제출하고 채점 결과를 받습니다.")
    @Parameter(name = "submission_id", description = "답안지 ID", example = "1")
    public ResponseEntity<QuizResultDto> completeQuiz(
            @PathVariable("submission_id") Long submissionId,
            @Parameter(hidden = true) @CurrentUser User user,
            @Valid @RequestBody QuizSubmissionRequestDto request
    ) {
        QuizResultDto result = quizService.completeQuiz(submissionId, user, request.answers());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/submissions/{submission_id}/result")
    @Operation(
            summary = "퀴즈 결과 조회",
            description = "사용자가 제출한 퀴즈 답안의 채점 결과를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "퀴즈 결과 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "제출된 답안지를 찾을 수 없음")
            }
    )
    @Parameter(name = "submission_id", description = "답안지 ID", example = "1")
    public ResponseEntity<QuizResultDto> getQuizResult(@PathVariable("submission_id") Long submissionId,
                                                       @Parameter(hidden = true) @CurrentUser User user) {
        QuizResultDto result = quizQueryService.getSubmissionResult(submissionId, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/quizzes/{quiz_id}")
    @Operation(summary = "퀴즈 삭제", description = "특정 퀴즈를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "퀴즈 없음")
            })
    @Parameter(name = "quiz_id", description = "퀴즈 ID", example = "1")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable("quiz_id") Long quizId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        quizService.deleteQuiz(quizId, user);
        return ResponseEntity.noContent().build();
    }
}