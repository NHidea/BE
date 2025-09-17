package com.NH.Fintech.config;

import com.NH.Fintech.controller.dto.ErrorResponse;
import com.NH.Fintech.exception.BadRequestException;
import com.NH.Fintech.exception.BusinessException;
import com.NH.Fintech.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // 404 - 도메인 NotFound
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400 - 도메인 유효성 위반
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 커스텀 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return build(ex.getStatus(), ex.getMessage());
    }

    // 400 - @Valid 바인딩 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, msg.isEmpty() ? "잘못된 요청입니다." : msg);
    }

    // 400 - @Validated 제약 위반(@PathVariable/@RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, msg.isEmpty() ? "잘못된 요청입니다." : msg);
    }

    // 400 - 필수 파라미터 누락/타입 불일치/JSON 파싱/바인딩
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            BindException.class
    })
    public ResponseEntity<ErrorResponse> handleBadInput(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, friendlyMessage(ex));
    }

    // 404 - 매핑 없음 (throw-exception-if-no-handler-found=true 필요)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "요청하신 경로를 찾을 수 없습니다: " + ex.getRequestURL());
    }

    // 405 / 415
    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ErrorResponse> handleMethodMedia(Exception ex) {
        HttpStatus status = (ex instanceof HttpRequestMethodNotSupportedException)
                ? HttpStatus.METHOD_NOT_ALLOWED
                : HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        return build(status, ex.getMessage());
    }

    // 409 - 무결성 제약
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "데이터 제약 조건을 위반했습니다.");
    }

    // 500 - 그 외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaught(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다.");
    }

    // ===== helpers =====
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase().replace(' ', '_').toUpperCase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private String friendlyMessage(Exception ex) {
        if (ex instanceof MissingServletRequestParameterException e) {
            return "필수 파라미터가 없습니다: " + e.getParameterName();
        }
        if (ex instanceof MethodArgumentTypeMismatchException e) {
            return "파라미터 타입이 올바르지 않습니다: " + e.getName();
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "요청 본문을 읽을 수 없습니다(형식 오류).";
        }
        if (ex instanceof BindException e) {
            String msg = e.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return msg.isEmpty() ? "잘못된 요청입니다." : msg;
        }
        return "잘못된 요청입니다.";
    }
}
