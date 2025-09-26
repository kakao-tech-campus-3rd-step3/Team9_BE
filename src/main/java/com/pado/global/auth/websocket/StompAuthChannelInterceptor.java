package com.pado.global.auth.websocket;

import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.auth.jwt.JwtProvider;
import com.pado.global.auth.userdetails.CustomUserDetails;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final StudyMemberRepository studyMemberRepository;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateUser(accessor);
        } else if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateSubscription(accessor);
        }

        return message;
    }

    // 소켓 연결 전 헤더에서 토큰을 가져와 검증
    private void authenticateUser(StompHeaderAccessor accessor) {
        String authToken = accessor.getFirstNativeHeader("Authorization");

        if (!StringUtils.hasText(authToken) || !authToken.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER);
        }

        String jwt = authToken.substring(BEARER_PREFIX.length());

        try {
            Long userId = jwtProvider.getUserId(jwt);
            Optional<User> userOptional = userRepository.findWithInterestsById(userId);

            if (userOptional.isEmpty()) {
                throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER);
            }

            User user = userOptional.get();
            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails, null, customUserDetails.getAuthorities());

            accessor.setUser(authentication);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.WEBSOCKET_CONNECTION_FAILED);
        }
    }

    // 채팅방 구독 전 1차 검증 (스터디원에 속하는지, 스터디가 존재하는지)
    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        // 스터디 채팅방 구독일 때만 검증 수행
        if (destination != null && destination.startsWith("/topic/studies/")) {
            Long studyId = extractStudyIdFromDestination(destination);

            Authentication auth = (Authentication) accessor.getUser();
            if (auth == null) {
                throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER);
            }

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();

            if (!studyMemberRepository.existsByStudyIdAndUserId(studyId, userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
            }
        }
    }

    private Long extractStudyIdFromDestination(String destination) {
        Pattern pattern = Pattern.compile("/topic/studies/(\\d+)/chats");
        Matcher matcher = pattern.matcher(destination);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        
        throw new BusinessException(ErrorCode.INVALID_SUBSCRIBE_PATH);
    }
}