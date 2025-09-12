package com.pado.global.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pado.global.auth.userdetails.CustomUserDetailsService;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.global.exception.dto.ErrorResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            String header = req.getHeader(AUTH_HEADER);

            if (header == null || header.isBlank()) {
                chain.doFilter(req, res);
                return;
            }
            if (!header.startsWith(BEARER)) {
                writeUnauthorized(res, ErrorCode.TOKEN_INVALID, ErrorCode.TOKEN_INVALID.message, req.getRequestURI());
                return;
            }

            if (header != null && header.startsWith(BEARER)) {
                String token = header.substring(BEARER.length());

                jwtProvider.validate(token);
                Long userId = jwtProvider.getUserId(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                Authentication auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(req, res);

        }
        catch (BusinessException ex) {
            writeUnauthorized(res, ex.getErrorCode(), ex.getMessage(), req.getRequestURI());
        }
        catch (Exception ex) {
            writeUnauthorized(res, ErrorCode.TOKEN_INVALID, ErrorCode.TOKEN_INVALID.message, req.getRequestURI());
        }
    }

    private void writeUnauthorized(HttpServletResponse res, ErrorCode code, String message, String path) throws IOException {
        if (res.isCommitted()) return;

        res.setStatus(code.status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = objectMapper.writeValueAsString(
                ErrorResponseDto.of(code, message, java.util.Collections.emptyList(), path)
        );
        res.getWriter().write(body);
    }
}
