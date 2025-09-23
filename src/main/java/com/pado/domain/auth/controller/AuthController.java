package com.pado.domain.auth.controller;

import com.pado.domain.auth.dto.request.EmailSendRequestDto;
import com.pado.domain.auth.dto.request.EmailVerifyRequestDto;
import com.pado.domain.auth.dto.request.LoginRequestDto;
import com.pado.domain.auth.dto.request.SignUpRequestDto;
import com.pado.domain.auth.dto.response.EmailVerificationResponseDto;
import com.pado.domain.auth.dto.response.NicknameCheckResponseDto;
import com.pado.domain.auth.dto.response.TokenResponseDto;
import com.pado.domain.auth.dto.response.TokenWithRefreshResponseDto;
import com.pado.domain.auth.service.AuthService;
import com.pado.global.auth.jwt.JwtProvider;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.global.exception.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "01. Authentication", description = "인증/인가 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private boolean isLocalLike() {
        String p = activeProfile == null ? "" : activeProfile.toLowerCase();
        return p.contains("dev") || p.contains("local");
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NicknameCheckResponseDto.class),
                examples = {
                    @ExampleObject(name = "사용 가능", value = "{\"isAvailable\":true}"),
                    @ExampleObject(name = "사용 불가", value = "{\"isAvailable\":false}")
                })),
        @ApiResponse(responseCode = "400", description = "유효성 오류",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(name = "유효성 에러", value = "{\"code\":\"INVALID_INPUT\",\"message\":\"입력값이 올바르지 않습니다.\",\"errors\":[\"nickname: 2~10자\"],\"path\":\"/api/auth/check-nickname\"}")))
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameCheckResponseDto> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(authService.checkNickname(nickname));
    }

    @Operation(summary = "회원가입", description = "이메일/비밀번호 등으로 회원가입합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "생성"),
        @ApiResponse(responseCode = "409", description = "중복 이메일/닉네임",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = {
                    @ExampleObject(name = "이메일 중복", value = "{\"code\":\"DUPLICATE_EMAIL\",\"message\":\"이미 사용 중인 이메일입니다.\",\"errors\":[],\"path\":\"/api/auth/signup\"}"),
                    @ExampleObject(name = "닉네임 중복", value = "{\"code\":\"DUPLICATE_NICKNAME\",\"message\":\"이미 사용 중인 닉네임입니다.\",\"errors\":[],\"path\":\"/api/auth/signup\"}")
                }))
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> register(@Valid @RequestBody SignUpRequestDto request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고, Refresh 쿠키를 설정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(name = "자격 증명 불일치", value = "{\"code\":\"UNAUTHENTICATED_USER\",\"message\":\"Invalid credentials\",\"errors\":[],\"path\":\"/api/auth/login\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        TokenWithRefreshResponseDto tokens = authService.login(request);

        ResponseCookie.ResponseCookieBuilder base = ResponseCookie.from("REFRESH_TOKEN",
                tokens.refreshToken())
            .httpOnly(true)
            .path("/")
            .maxAge(Duration.ofSeconds(jwtProvider.getRefreshTtl()));

        ResponseCookie cookie = isLocalLike()
            ? base.secure(false).sameSite("Lax").build()
            : base.domain(".gogumalatte.site").secure(true).sameSite("None").build();
        org.slf4j.LoggerFactory.getLogger(AuthController.class)
            .info("Login Set-Cookie => {}", cookie.toString());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(new TokenResponseDto(tokens.accessToken()));
    }

    @Operation(summary = "액세스 토큰 재발급", description = "Refresh 쿠키로 Access 토큰을 재발급합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "리프레시 누락/불일치",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = {
                    @ExampleObject(name = "쿠키 누락", value = "{\"code\":\"UNAUTHENTICATED_USER\",\"message\":\"RefreshToken 쿠키가 없습니다.\",\"errors\":[],\"path\":\"/api/auth/refresh\"}"),
                    @ExampleObject(name = "토큰 불일치", value = "{\"code\":\"UNAUTHENTICATED_USER\",\"message\":\"RefreshToken mismatch\",\"errors\":[],\"path\":\"/api/auth/refresh\"}")
                }))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(
        @CookieValue(value = "REFRESH_TOKEN", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER, "RefreshToken 쿠키가 없습니다.");
        }
        return ResponseEntity.ok(authService.renewAccessToken(refreshToken));
    }

    @Operation(summary = "이메일 인증코드 발송", description = "이메일로 인증 코드를 보냅니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = EmailVerificationResponseDto.class))),
        @ApiResponse(responseCode = "409", description = "중복 이메일(정책상 충돌 상황 가정)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(name = "이메일 충돌", value = "{\"code\":\"DUPLICATE_EMAIL\",\"message\":\"이미 사용 중인 이메일입니다.\",\"errors\":[],\"path\":\"/api/auth/email/send\"}")))
    })
    @PostMapping("/email/send")
    public ResponseEntity<EmailVerificationResponseDto> sendVerificationEmail(
        @Valid @RequestBody EmailSendRequestDto request) {
        return ResponseEntity.ok(authService.emailSend(request));
    }

    @Operation(summary = "이메일 인증", description = "인증 코드를 검증합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = EmailVerificationResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "코드 불일치",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(name = "코드 불일치", value = "{\"code\":\"VERIFICATION_CODE_MISMATCH\",\"message\":\"인증 코드가 올바르지 않습니다.\",\"errors\":[],\"path\":\"/api/auth/email/verify\"}")))
    })
    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerificationResponseDto> verifyEmailCode(
        @Valid @RequestBody EmailVerifyRequestDto request) {
        return ResponseEntity.ok(authService.emailVerify(request));
    }
}
