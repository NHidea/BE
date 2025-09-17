package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "계좌 응답 DTO")
public class AccountResponse {

    @Schema(description = "계좌 PK", example = "101")
    private Long accountId;

    @Schema(description = "소유자 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "은행 이름", example = "NH농협은행")
    private String bankName;

    @Schema(description = "계좌번호", example = "301-1234-5678-90")
    private String accountNumber;

    @Schema(description = "계좌 잔액", example = "150000.50")
    private BigDecimal balance;
}
