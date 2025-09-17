package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "소비 로그 생성 요청 DTO")
public class CreateConsumptionRequest {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "소비 금액", example = "12000.00")
    private BigDecimal amount;

    @Schema(description = "소비 내역", example = "점심 김밥천국")
    private String content;

    @Schema(description = "거래 일시 (ISO-8601)",
            example = "2025-09-16T10:30:00")
    private LocalDateTime transactionAt;
}
