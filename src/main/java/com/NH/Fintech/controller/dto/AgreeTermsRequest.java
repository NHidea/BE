package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "약관 동의 요청 DTO")
public class AgreeTermsRequest {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "약관 코드", example = "PRIVACY01")
    private String termsCode;

    @Schema(description = "필수 약관 여부", example = "true")
    private boolean required;

    @Schema(description = "동의 여부", example = "true")
    private boolean agreed;
}
