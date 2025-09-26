package com.pado.global.exception.common;

import com.pado.global.exception.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    // 명시적 비즈니스 예외 처리
    @MessageExceptionHandler(BusinessException.class)
    @SendToUser("/queue/errors") // 에러를 발생시킨 사용자에게만 전송
    public ErrorResponseDto handleBusinessException(BusinessException e, StompHeaderAccessor accessor, Principal principal) {
        ErrorCode code = e.getErrorCode();
        String destination = getDestinationSafely(accessor);

        return ErrorResponseDto.of(code, e.getMessage(), Collections.emptyList(), destination);
    }

    // DTO 검증 실패
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public ErrorResponseDto handleMethodArgumentNotValid(MethodArgumentNotValidException e, StompHeaderAccessor accessor) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorCode code = ErrorCode.INVALID_INPUT;
        String destination = getDestinationSafely(accessor);

        log.warn("Validation error on destination {}: {}", accessor.getDestination(), errors);

        return ErrorResponseDto.of(code, e.getMessage(), errors, destination);
    }

    // 마지막 안전망
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorResponseDto handleUnexpectedException(Exception e, StompHeaderAccessor accessor, Principal principal) {
        String userId = getPrincipalName(principal);
        String destination = getDestinationSafely(accessor);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;

        log.error("WebSocket error - User: {}, Destination: {}, Error: {}",
                userId, destination, e.getMessage(), e);

        return ErrorResponseDto.of(code, e.getMessage(), Collections.emptyList(), destination);
    }

    private String getDestinationSafely(StompHeaderAccessor accessor) {
        return accessor != null && accessor.getDestination() != null
                ? accessor.getDestination()
                : "UNKNOWN";
    }

    private String getPrincipalName(Principal principal) {
        return principal != null ? principal.getName() : "ANONYMOUS";
    }
}