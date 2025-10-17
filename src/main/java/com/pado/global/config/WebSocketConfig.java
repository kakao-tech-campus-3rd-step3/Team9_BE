package com.pado.global.config;

import com.pado.global.auth.resolver.WebSocketCurrentUserArgumentResolver;
import com.pado.global.auth.websocket.StompAuthChannelInterceptor;
import com.pado.global.exception.common.WebSocketExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
    private final WebSocketCurrentUserArgumentResolver webSocketCurrentUserArgumentResolver;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트에서 메시지를 구독할 때 사용할 prefix
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 클라이언트에서 메시지를 전송할 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");

        // 개인 메세지용 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 설정
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // ChannelInterceptor 등록
        registration.interceptors(stompAuthChannelInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // WebSocket용 ArgumentResolver 등록
        argumentResolvers.add(webSocketCurrentUserArgumentResolver);
    }
}