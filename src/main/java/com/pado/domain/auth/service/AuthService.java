package com.pado.domain.auth.service;

import com.pado.domain.auth.dto.request.LoginRequestDto;
import com.pado.domain.auth.dto.request.SignUpRequestDto;
import com.pado.domain.auth.dto.response.TokenResponseDto;

public interface AuthService {
    //회원가입
    void register(SignUpRequestDto request);

    //로그인
    TokenResponseDto login(LoginRequestDto request);
}
