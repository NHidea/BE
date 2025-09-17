package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 MBTI 답변 응답 DTO")
public class UserMbtiAnswerResponse {

    @Schema(description = "답변 ID", example = "801")
    private Long answerId;

    @Schema(description = "질문 ID", example = "101")
    private Long questionId;

    @Schema(description = "선택지 ID", example = "301")
    private Long optionId;

    @Schema(description = "선택지 코드", example = "I")
    private String optionCode;

    @Schema(description = "답변 시간", example = "2025-09-16T10:45:00")
    private LocalDateTime answeredAt;
}
