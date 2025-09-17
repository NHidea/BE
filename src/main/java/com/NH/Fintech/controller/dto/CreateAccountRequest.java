package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "계좌 생성 요청 DTO")
public class CreateAccountRequest {

    @Schema(description = "소유자 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "은행 이름", example = "NH농협은행")
    private String bankName;

    @Schema(description = "계좌번호", example = "301-9876-5432-10")
    private String accountNumber;

    @Schema(description = "초기 입금 금액", example = "50000.00")
    private BigDecimal balance;
}
