package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "이용 약관 동의 응답 DTO")
public class TermsAgreementResponse {

    @Schema(description = "동의 ID", example = "501")
    private Long agreementId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "약관 코드", example = "TOS_001")
    private String termsCode;

    @Schema(description = "필수 여부", example = "true")
    private boolean required;

    @Schema(description = "동의 여부", example = "true")
    private boolean agreed;

    @Schema(description = "동의 일시", example = "2025-09-16T12:45:00")
    private LocalDateTime agreedAt;
}
