package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "AI 제안 커밋 요청")
public class SuggestCommitRequest {

    @Schema(description = "대상 사용자", example = "1")
    private Long userId;

    @Schema(description = "하루 단위 날짜", example = "2025-09-16")
    private LocalDate periodDate;

    @Schema(description = "중복 방지용 요청 ID(선택)", example = "2025-09-16-user1-ai-001")
    private String requestId;

    @Schema(description = "확정할 제안 목록(최대 3개)")
    private List<AiTodoSuggestion> suggestions;
}
