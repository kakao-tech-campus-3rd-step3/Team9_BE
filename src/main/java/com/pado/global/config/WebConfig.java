package com.pado.global.config;

import com.pado.global.auth.JwtAuthInterceptor;
import com.pado.global.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final JwtProvider jwtProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAuthInterceptor(jwtProvider))
                .addPathPatterns("/api/**")           // JWT 검사 적용할 경로
                .excludePathPatterns("/api/auth/**"); // 예외: 로그인/회원가입
    }
}
