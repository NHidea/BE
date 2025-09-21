package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "AI가 제안한 투두(미리보기용)")
public class AiTodoSuggestion {

    private Long userId;
    private LocalDate periodDate;

    @Schema(description = "제목(필수)")
    private String title;

    private String summary;
    private Integer orderIndex;

    private String ruleCode;
    private String ruleParams;
}
