package com.pado.global.auth;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;

    public JwtAuthInterceptor(JwtProvider jwtProvider) { this.jwtProvider = jwtProvider; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER);
        }
        String token = auth.substring(7);
        jwtProvider.validate(token);
        Long userId = jwtProvider.getUserId(token);
        req.setAttribute("userId", userId); //controller에서 httpServletRequest req.getAttribute("userId")로 사용가능
        return true;
    }
}
