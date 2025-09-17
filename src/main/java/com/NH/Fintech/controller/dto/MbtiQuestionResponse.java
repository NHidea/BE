package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "MBTI 질문 응답 DTO")
public class MbtiQuestionResponse {

    @Schema(description = "질문 ID", example = "101")
    private Long questionId;

    @Schema(description = "질문 코드", example = "I_E_Q1")
    private String questionCode;

    @Schema(description = "질문 제목", example = "외향형/내향형을 구분하는 질문")
    private String title;

    @Schema(description = "질문 내용", example = "모임 후 기분이 어떤가요?")
    private String content;

    @Schema(description = "표시 순서", example = "1")
    private Integer displayOrder;
}
