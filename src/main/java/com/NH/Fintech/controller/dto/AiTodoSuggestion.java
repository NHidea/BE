package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "AI가 제안한 투두(미리보기용)")
public class AiTodoSuggestion {
    @Schema(description = "대상 사용자", example = "1")
    private Long userId;

    @Schema(description = "하루 단위 날짜", example = "2025-09-16")
    private LocalDate periodDate;

    @Schema(description = "제목(필수)", example = "아침에 물 2잔 마시기")
    private String title;

    @Schema(description = "요약(선택)", example = "기상 직후 작은 컵 기준 2잔")
    private String summary;

    @Schema(description = "정렬 순서(선택)", example = "1")
    private Integer orderIndex;
}
