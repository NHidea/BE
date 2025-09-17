package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "에러 응답 DTO")
public class ErrorResponse {

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 유형", example = "BAD_REQUEST")
    private String error;

    @Schema(description = "상세 메시지", example = "필수 파라미터 userId가 누락되었습니다.")
    private String message;

    @Schema(description = "발생 시각 (ISO-8601)", example = "2025-09-16T11:05:00")
    private LocalDateTime timestamp;
}
