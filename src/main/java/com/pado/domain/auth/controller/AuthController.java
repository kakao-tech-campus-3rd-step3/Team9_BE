package com.pado.domain.auth.controller;

import com.pado.domain.auth.dto.request.EmailSendRequestDto;
import com.pado.domain.auth.dto.request.EmailVerifyRequestDto;
import com.pado.domain.auth.dto.request.LoginRequestDto;
import com.pado.domain.auth.dto.request.SignUpRequestDto;
import com.pado.domain.auth.dto.response.NicknameCheckResponseDto;
import com.pado.domain.auth.dto.response.EmailVerificationResponseDto;
import com.pado.domain.auth.dto.response.TokenResponseDto;
import com.pado.domain.auth.service.AuthService;
import com.pado.global.exception.dto.ErrorResponseDto;
import com.pado.global.swagger.annotation.common.NoApi409Conflict;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01. Authentication", description = "로그인, 회원가입 등 사용자 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@SecurityRequirements({})
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @Operation(summary = "닉네임 중복 확인", description = "입력한 닉네임이 사용 가능한지 확인합니다.\n\n" +
            "**[Mock 테스트용 안내]**\n" +
            "- `nickname`으로 \"중복닉네임\"을 전송하면 `false`를 반환합니다.\n" +
            "- 그 외 모든 값에 대해서는 `true`를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능 여부 확인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NicknameCheckResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "사용 가능한 닉네임", value = "{\"is_available\": true}"),
                                    @ExampleObject(name = "이미 존재하는 닉네임", value = "{\"is_available\": false}")
                            })),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 입력 값",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    name = "파라미터 유효성 오류",
                                    value = """
                                            {
                                              "code": "INVALID_INPUT",
                                              "message": "요청 값이 올바르지 않습니다.",
                                              "errors": [
                                                "nickname: 닉네임은 2자 이상 10자 이하로 입력해주세요."
                                              ],
                                              "timestamp": "2025-09-07T08:15:10.8668626",
                                              "path": "/api/auth/check-nickname"
                                            }
                                            """
                            )
                    )
            )
    })
    @Parameters({
            @Parameter(name = "nickname", description = "중복 확인할 닉네임", required = true, example = "파도")
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameCheckResponseDto> checkNickname(
            @RequestParam
            @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
            String nickname
    ) {
        return ResponseEntity.ok(authService.checkNickname(nickname));
    }

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 성별, 관심 분야, 지역 정보를 입력 받아 회원 계정을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 닉네임",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "이미 존재하는 이메일",
                                            value = """
                                                    {
                                                      "code": "DUPLICATE_EMAIL",
                                                      "message": "이미 사용 중인 이메일입니다.",
                                                      "errors": [],
                                                      "timestamp": "2025-09-07T08:15:10.8668626",
                                                      "path": "/api/auth/signup"
                                                    }
                                                    """),
                                    @ExampleObject(name = "이미 존재하는 닉네임",
                                            value = """
                                                    {
                                                      "code": "DUPLICATE_NICKNAME",
                                                      "message": "이미 사용 중인 닉네임입니다.",
                                                      "errors": [],
                                                      "timestamp": "2025-09-07T08:15:10.8668626",
                                                      "path": "/api/auth/signup"
                                                    }
                                                    """)
                            })
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> register(@Valid @RequestBody SignUpRequestDto request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @NoApi409Conflict
    @Operation(summary = "로그인", description = "사용자가 이메일과 비밀번호를 입력해 로그인을 요청합니다. 입력 정보가 맞으면 인증 토큰(예: JWT)을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    name = "인증 실패",
                                    value = """
                                            {
                                              "code": "AUTHENTICATION_FAILED",
                                              "message": "이메일 또는 비밀번호가 일치하지 않습니다.",
                                              "errors": [],
                                              "timestamp": "2025-09-07T08:15:10.8668626",
                                              "path": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @NoApi409Conflict
    @Operation(summary = "이메일 인증(인증번호 전송)", description = "입력한 이메일로 인증번호를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증번호 전송 성공",
                    content = @Content(schema = @Schema(implementation = EmailVerificationResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"인증번호가 성공적으로 전송되었습니다.\"}"))),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "이미 가입된 이메일",
                                    value = """
                                            {
                                              "code": "DUPLICATE_EMAIL",
                                              "message": "이미 사용 중인 이메일입니다.",
                                              "errors": [],
                                              "timestamp": "2025-09-07T08:15:10.8668626",
                                              "path": "/api/auth/email/send"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/email/send")
    public ResponseEntity<EmailVerificationResponseDto> sendVerificationEmail(@Valid @RequestBody EmailSendRequestDto request) {
        return ResponseEntity.ok(authService.emailSend(request));
    }

    @NoApi409Conflict
    @Operation(summary = "이메일 인증(인증번호 확인)", description = "사용자가 입력한 인증번호가 올바른지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공",
                    content = @Content(schema = @Schema(implementation = EmailVerificationResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"이메일 인증이 완료되었습니다.\"}"))),
            @ApiResponse(responseCode = "401", description = "인증번호 불일치 또는 만료",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "인증번호 불일치 또는 만료",
                                    value = """
                                            {
                                              "code": "VERIFICATION_CODE_MISMATCH",
                                              "message": "인증번호가 일치하지 않거나 만료되었습니다.",
                                              "errors": [],
                                              "timestamp": "2025-09-07T08:15:10.8668626",
                                              "path": "/api/auth/email/verify"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerificationResponseDto> verifyEmailCode(@Valid @RequestBody EmailVerifyRequestDto request) {
        return ResponseEntity.ok(authService.emailVerify(request));
    }
}
