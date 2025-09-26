package com.pado.domain.quiz.controller;

import com.pado.domain.quiz.dto.request.QuizCreateRequestDto;
import com.pado.domain.quiz.dto.request.QuizSubmissionRequestDto;
import com.pado.domain.quiz.dto.response.CursorResponseDto;
import com.pado.domain.quiz.dto.response.QuizDetailDto;
import com.pado.domain.quiz.dto.response.QuizInfoDto;
import com.pado.domain.quiz.dto.response.QuizProgressDto;
import com.pado.domain.quiz.dto.response.QuizResultDto;
import com.pado.domain.quiz.service.QuizGenerationService;
import com.pado.domain.quiz.service.QuizService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "10. Quiz", description = "퀴즈 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizGenerationService quizGenerationService;

    @PostMapping("/studies/{studyId}/quizzes")
    @Operation(summary = "AI 퀴즈 생성 요청", description = "선택된 파일들과 제목으로 AI 퀴즈 생성을 요청합니다. 비동기로 처리됩니다.")
    public ResponseEntity<Void> createQuiz(
            @PathVariable Long studyId,
            @Parameter(hidden = true) @CurrentUser User user,
            @Valid @RequestBody QuizCreateRequestDto request
    ) {
        quizGenerationService.generateQuiz(user, request.title(), request.fileIds(), studyId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/studies/{studyId}/quizzes")
    @Operation(summary = "스터디 퀴즈 목록 조회", description = "스터디에 속한 퀴즈 목록을 무한 스크롤 방식으로 조회합니다.")
    public ResponseEntity<CursorResponseDto<QuizInfoDto>> getQuizzes(
            @PathVariable Long studyId,
            @Parameter(hidden = true) @CurrentUser User user,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        CursorResponseDto<QuizInfoDto> response = quizService.findQuizzesByStudy(studyId, user, cursor, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/quizzes/{quizId}")
    @Operation(summary = "퀴즈 상세 조회", description = "사용자가 풀 퀴즈의 상세 정보(문제, 보기)를 조회합니다.")
    public ResponseEntity<QuizDetailDto> getQuizDetail(
            @PathVariable Long quizId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        QuizDetailDto quizDetail = quizService.getQuizDetail(quizId, user);
        return ResponseEntity.ok(quizDetail);
    }

    @PostMapping("/quizzes/{quizId}/submissions")
    @Operation(summary = "퀴즈 풀기 시작", description = "퀴즈 풀기를 시작하고, 진행 중인 답안지가 있으면 그 정보를 반환합니다.")
    public ResponseEntity<QuizProgressDto> startQuiz(
            @PathVariable Long quizId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        QuizProgressDto response = quizService.startQuiz(quizId, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submissions/{submissionId}/complete")
    @Operation(summary = "퀴즈 답안 제출", description = "진행 중인 퀴즈의 답안을 최종 제출하고 채점 결과를 받습니다.")
    public ResponseEntity<QuizResultDto> completeQuiz(
            @PathVariable Long submissionId,
            @Parameter(hidden = true) @CurrentUser User user,
            @Valid @RequestBody QuizSubmissionRequestDto request
    ) {
        QuizResultDto result = quizService.completeQuiz(submissionId, user, request.answers());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/quizzes/{quizId}/regenerate")
    @Operation(summary = "실패한 퀴즈 재생성 요청", description = "생성에 실패한 퀴즈에 대해 재생성을 요청합니다.")
    public ResponseEntity<Void> requestQuizRegeneration(
            @PathVariable Long quizId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        quizService.requestQuizRegeneration(quizId, user);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/quizzes/{quizId}")
    @Operation(summary = "퀴즈 삭제 (팀장 권한)", description = "특정 퀴즈를 삭제합니다.")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        quizService.deleteQuiz(quizId, user);
        return ResponseEntity.noContent().build();
    }
}