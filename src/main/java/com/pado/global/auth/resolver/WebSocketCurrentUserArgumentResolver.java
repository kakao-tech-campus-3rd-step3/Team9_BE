package com.pado.global.auth.resolver;

import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.auth.userdetails.CustomUserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver; // messaging용 인터페이스
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        // ChannelInterceptor에서 설정한 사용자 정보를 Principal 형태로 가져옴
        Principal principal = StompHeaderAccessor.getUser(message.getHeaders());

        if (principal == null) {
            return null;
        }

        // Principal 객체가 우리가 설정한 Authentication 타입인지 확인
        if (principal instanceof Authentication && ((Authentication) principal).getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) ((Authentication) principal).getPrincipal();
            return userDetails.getUser();
        }

        return null;
    }
}