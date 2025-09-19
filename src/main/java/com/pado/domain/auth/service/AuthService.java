package com.pado.domain.auth.service;

import com.pado.domain.auth.dto.request.EmailSendRequestDto;
import com.pado.domain.auth.dto.request.EmailVerifyRequestDto;
import com.pado.domain.auth.dto.request.LoginRequestDto;
import com.pado.domain.auth.dto.request.SignUpRequestDto;
import com.pado.domain.auth.dto.response.EmailVerificationResponseDto;
import com.pado.domain.auth.dto.response.NicknameCheckResponseDto;
import com.pado.domain.auth.dto.response.TokenResponseDto;
import jakarta.validation.Valid;

public interface AuthService {
    //회원가입
    void register(SignUpRequestDto request);

    //로그인
    TokenResponseDto login(LoginRequestDto request);

    //닉네임 중복 확인
    NicknameCheckResponseDto  checkNickname(String nickname);

    //이메일 인증번호 전송
    EmailVerificationResponseDto emailSend(EmailSendRequestDto request);

    //이메일 인증번호 검증
    EmailVerificationResponseDto emailVerify(@Valid EmailVerifyRequestDto request);
}
