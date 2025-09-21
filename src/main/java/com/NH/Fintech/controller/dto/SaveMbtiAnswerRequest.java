package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "사용자 MBTI 답변 저장 요청 DTO")
public class SaveMbtiAnswerRequest {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "질문 ID", example = "2") // 예: Q2
    private Long questionId;

    @Schema(description = "선택지 ID", example = "10") // 예: mbti_option.option_id
    private Long optionId;

    @Schema(description = "선택지 코드(Qx_A~D 형식)", example = "Q2_C")
    private String optionCode;
}
