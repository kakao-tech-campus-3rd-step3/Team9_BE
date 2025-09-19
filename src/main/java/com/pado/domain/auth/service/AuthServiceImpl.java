package com.pado.domain.auth.service;

import com.pado.domain.auth.dto.request.EmailSendRequestDto;
import com.pado.domain.auth.dto.request.EmailVerifyRequestDto;
import com.pado.domain.auth.dto.request.LoginRequestDto;
import com.pado.domain.auth.dto.request.SignUpRequestDto;
import com.pado.domain.auth.dto.response.EmailVerificationResponseDto;
import com.pado.domain.auth.dto.response.NicknameCheckResponseDto;
import com.pado.domain.auth.dto.response.TokenResponseDto;
import com.pado.domain.auth.entity.EmailVerification;
import com.pado.domain.auth.mail.MailClient;
import com.pado.domain.auth.repository.EmailVerificationRepository;
import com.pado.domain.shared.entity.Category;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.auth.jwt.JwtProvider;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MailClient mailClient;

    @Override
    public void register(@Valid SignUpRequestDto request) {
        if(userRepository.existsByEmail(request.email())){
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordHash)
                .nickname(request.nickname())
                .region(request.region())
                .profileImageUrl(request.image_url())
                .gender(request.gender())
                .build();

        for(Category interest: request.interests())
            user.addInterest(interest);

        userRepository.save(user);
    }

    @Override
    public TokenResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.UNAUTHENTICATED_USER, "이메일 또는 비밀번호가 일치하지 않습니다."));
        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER, "이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        return new TokenResponseDto(accessToken);
    }

    @Override
    public NicknameCheckResponseDto checkNickname(String nickname) {
        if(userRepository.existsByNickname(nickname)){
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        return new NicknameCheckResponseDto(true);
    }

    private String generateCode() {
        SecureRandom r = new SecureRandom();
        int n = r.nextInt(1_000_000); // 0 ~ 999999
        return String.format("%06d", n);
    }

    private String buildMailBody(String code) {
        return """
                안녕하세요. 인증번호를 안내드립니다.

                인증번호: %s

                본 메일을 요청하지 않았다면 무시하셔도 됩니다.
                """.formatted(code);
    }

    @Override
    public EmailVerificationResponseDto emailSend(EmailSendRequestDto request) {
        String code = generateCode();
        emailVerificationRepository.save(EmailVerification.builder()
                        .email(request.email())
                        .code(code)
                        .build()
        );
        mailClient.send(request.email(), "[PADO] 이메일 인증번호", buildMailBody(code));

        return new EmailVerificationResponseDto(true, "인증번호가 성공적으로 전송되었습니다.");
    }

    @Override
    public EmailVerificationResponseDto emailVerify(EmailVerifyRequestDto request) {
        EmailVerification emailVerification= emailVerificationRepository.findByEmail(request.email()).
                orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if(!request.verification_code().equals(emailVerification.getCode()))
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);

        return new EmailVerificationResponseDto(true, "이메일 인증이 완료되었습니다.");
    }


}
