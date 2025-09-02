package com.pado.global.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // 명시적 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest req) {
        ErrorCode code = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.of(code, ex.getMessage(), null, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // DTO 검증 실패 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(MethodArgumentNotValidException ex, WebRequest req) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .toList();
        ErrorCode code = ErrorCode.INVALID_INPUT;
        ErrorResponse body = ErrorResponse.of(code, code.message, errors, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // JSON 파싱 실패 등 Body 해석 불가
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, WebRequest req) {
        ErrorCode code = ErrorCode.JSON_PARSE_ERROR;
        ErrorResponse body = ErrorResponse.of(code, code.message, null, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // DB 무결성 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest req) {
        ErrorCode code = ErrorCode.DUPLICATE_KEY;
        ErrorResponse body = ErrorResponse.of(code, code.message, null, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    // 마지막 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest req) {
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        ErrorResponse body = ErrorResponse.of(code, code.message, null, path(req));
        return ResponseEntity.status(code.status).body(body);
    }

    private static String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    private static String path(WebRequest req) {
        // ex) "uri=/api/users"
        String desc = req.getDescription(false);
        if (desc != null && desc.startsWith("uri=")) return desc.substring(4);
        return desc;
    }
}
