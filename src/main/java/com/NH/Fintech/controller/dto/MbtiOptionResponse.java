package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "MBTI 선택지 응답 DTO")
public class MbtiOptionResponse {

    @Schema(description = "선택지 ID", example = "301")
    private Long optionId;

    @Schema(description = "질문 ID", example = "1001")
    private Long questionId;

    @Schema(description = "선택지 코드", example = "I")
    private String optionCode;

    @Schema(description = "선택지 텍스트", example = "혼자 있는 시간을 선호한다")
    private String optionText;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder;
}
