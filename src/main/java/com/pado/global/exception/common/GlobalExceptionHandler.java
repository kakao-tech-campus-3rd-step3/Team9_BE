package com.pado.global.exception.common;

import com.pado.global.exception.dto.ErrorResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 명시적 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusiness(BusinessException ex, WebRequest req) {
        ErrorCode code = ex.getErrorCode();
        ErrorResponseDto body = ErrorResponseDto.of(code, ex.getMessage(), Collections.emptyList(),
            path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // DTO 검증 실패 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, WebRequest req) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(GlobalExceptionHandler::formatFieldError)
            .collect(Collectors.toList());

        ErrorCode code = ErrorCode.INVALID_INPUT;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, errors, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleHandlerMethodValidation(
        HandlerMethodValidationException ex, WebRequest req) {

        List<String> errors = ex.getParameterValidationResults().stream()
            .flatMap(result -> {
                // 파라미터 이름 확보 (컴파일 옵션에 따라 null일 수도 있음)
                String paramName = result.getMethodParameter().getParameterName();
                if (paramName == null) {
                    paramName = "arg" + result.getMethodParameter().getParameterIndex();
                }
                String finalParamName = paramName;
                return result.getResolvableErrors().stream()
                    .map(err -> finalParamName + ": " + err.getDefaultMessage());
            })
            .toList();

        ErrorCode code = ErrorCode.INVALID_INPUT;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, errors, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // 파라미터 유효성 검증 실패 (@RequestParam, @PathVariable)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
        ConstraintViolationException ex, WebRequest req) {
        List<String> errors = ex.getConstraintViolations().stream()
            .map(violation -> {
                String path = violation.getPropertyPath().toString();
                String paramName =
                    path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                return paramName + ": " + violation.getMessage();
            })
            .collect(Collectors.toList());

        ErrorCode code = ErrorCode.INVALID_INPUT;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, errors, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // 유효하지 않은 Enum 파라미터
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex, WebRequest req) {
        String requiredType =
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없는 타입";

        String message = String.format("요청 파라미터 '%s'의 값이 올바르지 않습니다.",
            ex.getName(), ex.getValue(), requiredType);

        ErrorCode code = ErrorCode.INVALID_INPUT;
        ErrorResponseDto body = ErrorResponseDto.of(code, message, Collections.emptyList(),
            path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // JSON 파싱 실패 등 Body 해석 불가
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleNotReadable(HttpMessageNotReadableException ex,
        WebRequest req) {
        Throwable cause = ex.getCause();
        Throwable rootCause = (cause != null) ? cause.getCause() : null;

        // Enum 변환 실패인 경우 별도로 처리
        if (rootCause instanceof IllegalArgumentException) {
            return buildInvalidEnumResponse((IllegalArgumentException) rootCause, req);
        }

        ErrorCode code = ErrorCode.JSON_PARSE_ERROR;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, Collections.emptyList(),
            path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidEnum(IllegalArgumentException ex,
        WebRequest req) {
        return buildInvalidEnumResponse(ex, req);
    }

    private ResponseEntity<ErrorResponseDto> buildInvalidEnumResponse(IllegalArgumentException ex,
        WebRequest req) {
        ErrorCode code = ErrorCode.INVALID_INPUT;
        List<String> errors = Collections.singletonList(ex.getMessage());

        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, errors, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // DB 무결성 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException ex,
        WebRequest req) {
        ErrorCode code = ErrorCode.DUPLICATE_KEY;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, Collections.emptyList(),
            path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // 마지막 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex, WebRequest req) {
        //로그
        String description = req.getDescription(false);
        String path = description.replace("uri=", "");
        log.error("Internal error at {}: {}", path, ex.getMessage(), ex);

        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, Collections.emptyList(),
            path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException ex,
        WebRequest req) {
        ErrorCode code = ErrorCode.ENTITY_NOT_FOUND;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, Collections.emptyList(),
            path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
        org.springframework.security.access.AccessDeniedException ex, WebRequest req) {
        ErrorCode code = ErrorCode.FORBIDDEN_REFLECTION_OWNER_ONLY;
        ErrorResponseDto body = ErrorResponseDto.of(code, code.message, Collections.emptyList(),
            path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    private static String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    private static String path(WebRequest req) {
        String desc = req.getDescription(false);
        if (desc != null && desc.startsWith("uri=")) {
            return desc.substring(4);
        }
        return desc;
    }
}