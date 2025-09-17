package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "소비 로그 응답 DTO")
public class ConsumptionLogResponse {

    @Schema(description = "소비 로그 PK", example = "501")
    private Long logId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "소비 금액", example = "12000.00")
    private BigDecimal amount;

    @Schema(description = "소비 내역 설명", example = "스타벅스 아메리카노")
    private String content;

    @Schema(description = "거래 일시 (ISO-8601)", example = "2025-09-16T14:20:00")
    private LocalDateTime transactionAt;

    @Schema(description = "연결된 Todo 아이템 ID(없을 수 있음)", example = "1001", nullable = true)
    private Long satisfiedTodoItemId;
}
