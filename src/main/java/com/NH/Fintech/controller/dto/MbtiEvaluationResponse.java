package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "MBTI 평가 응답 DTO")
public class MbtiEvaluationResponse {

    @Schema(description = "MBTI 배정 여부", example = "true")
    private boolean assigned;

    @Schema(description = "MBTI 코드", example = "INTJ")
    private String mbtiCode;

    @Schema(description = "캐릭터 ID", example = "2001")
    private Long characterId;

    @Schema(description = "캐릭터 이름", example = "전략가")
    private String characterName;
}
