package com.pado.domain.auth.service;

import com.pado.domain.auth.dto.request.EmailSendRequestDto;
import com.pado.domain.auth.dto.request.EmailVerifyRequestDto;
import com.pado.domain.auth.dto.request.LoginRequestDto;
import com.pado.domain.auth.dto.request.SignUpRequestDto;
import com.pado.domain.auth.dto.response.EmailVerificationResponseDto;
import com.pado.domain.auth.dto.response.NicknameCheckResponseDto;
import com.pado.domain.auth.dto.response.TokenResponseDto;
import com.pado.domain.auth.dto.response.TokenWithRefreshResponseDto;
import com.pado.domain.auth.infra.mail.MailClient;
import com.pado.domain.auth.infra.redis.RedisEmailVerificationStore;
import com.pado.domain.auth.infra.redis.RedisRefreshTokenStore;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.auth.jwt.JwtProvider;
import com.pado.global.config.AuthProps;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisEmailVerificationStore redisEmailVerificationStore;
    private final RedisRefreshTokenStore redisRefreshTokenStore;
    private final MailClient mailClient;
    private final AuthProps authProps;

    @Override
    public void register(@Valid SignUpRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.register(
            request.email(),
            passwordHash,
            request.nickname(),
            request.region(),
            request.image_key(),
            request.gender(),
            request.interests()
        );
        userRepository.save(user);
    }

    @Override
    public TokenWithRefreshResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(
                () -> new BusinessException(ErrorCode.UNAUTHENTICATED_USER, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER, "Invalid credentials");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getEmail());

        try {
            redisRefreshTokenStore.saveToken(user.getId(), refreshToken,
                Duration.ofSeconds(jwtProvider.getRefreshTtl()));
        } catch (DataAccessException e) {
            log.error(
                "Failed to save refresh token to Redis (userId={}, host/port check needed). reason={}, causeClass={}",
                user.getId(),
                e.getMessage(),
                (e.getCause() != null ? e.getCause().getClass().getName() : "null"),
                e);
            throw new BusinessException(ErrorCode.REDIS_UNAVAILABLE,
                "Redis unavailable while saving refresh token");
        }

        return new TokenWithRefreshResponseDto(accessToken, refreshToken);
    }

    @Override
    public TokenResponseDto renewAccessToken(String refreshToken) {
        Long userId = jwtProvider.getUserId(refreshToken);
        String savedToken = redisRefreshTokenStore.getToken(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED_USER,
                "RefreshToken not found"));
        if (!savedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER, "RefreshToken mismatch");
        }
        return new TokenResponseDto(
            jwtProvider.generateAccessToken(userId, jwtProvider.getEmail(refreshToken)));
    }

    @Override
    public NicknameCheckResponseDto checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        return new NicknameCheckResponseDto(true);
    }

    private String generateCode() {
        SecureRandom r = new SecureRandom();
        int n = r.nextInt(1_000_000); // 0 ~ 999999
        return String.format("%06d", n);
    }

    @Override
    public EmailVerificationResponseDto emailSend(EmailSendRequestDto request) {
        String code = generateCode();
        redisEmailVerificationStore.saveCode(request.email(), code,
            Duration.ofSeconds(authProps.getEmailVerificationTtl()));
        mailClient.send(request.email(), "[PADO] 이메일 인증 코드", buildMailBody(code));
        return new EmailVerificationResponseDto(true, "sent");
    }

    private String buildMailBody(String code) {
        return """
            안녕하세요, PADO 입니다.
            아래 6자리 인증 코드를 입력해 주세요.
            
            인증 코드: %s
            """.formatted(code);
    }

    @Override
    public EmailVerificationResponseDto emailVerify(@Valid EmailVerifyRequestDto request) {
        String code = redisEmailVerificationStore.getCode(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!code.equals(request.verification_code())) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }
        redisEmailVerificationStore.deleteCode(request.email());
        return new EmailVerificationResponseDto(true, "verified");
    }
}
