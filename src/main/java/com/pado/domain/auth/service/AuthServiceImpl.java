package com.pado.domain.auth.service;

import com.pado.domain.auth.dto.request.LoginRequestDto;
import com.pado.domain.auth.dto.request.SignUpRequestDto;
import com.pado.domain.auth.dto.response.TokenResponseDto;
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

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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


}
