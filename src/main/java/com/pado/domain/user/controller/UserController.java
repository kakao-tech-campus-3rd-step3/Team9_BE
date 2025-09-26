package com.pado.domain.user.controller;

import com.pado.domain.user.dto.UserDetailResponseDto;
import com.pado.domain.user.dto.UserSimpleResponseDto;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.service.UserService;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.swagger.annotation.user.Api404UserNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. User", description = "인증된 사용자 정보 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Api404UserNotFoundError
    @Operation(summary = "유저 정보 조회", description = "인증 토큰을 통해 현재 로그인된 사용자의 주요 정보(닉네임, 이미지 URL)를 조회합니다.")
    @ApiResponse(
            responseCode = "200", description = "유저 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = UserSimpleResponseDto.class))
    )
    @GetMapping
    public ResponseEntity<UserSimpleResponseDto> getSimpleUserInfo(@Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(userService.getUserSimple(user));
    }

    @Api404UserNotFoundError
    @Operation(summary = "유저 세부 정보 조회", description = "인증 토큰을 통해 현재 로그인된 사용자의 세부 정보(관심분야, 지역 포함)를 조회합니다.")
    @ApiResponse(
            responseCode = "200", description = "세부 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = UserDetailResponseDto.class))
    )
    @GetMapping("/detail")
    public ResponseEntity<UserDetailResponseDto> getDetailUserInfo(@Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(userService.getUserDetail(user.getId()));
    }
}