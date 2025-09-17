package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "AI 제안 미리보기 응답")
public class SuggestPreviewResponse {

    @Schema(description = "제안 3건")
    private List<AiTodoSuggestion> suggestions;

    @Schema(description = "제안 근거/소스", example = "mbti+spending")
    private String source;

    @Schema(description = "모델 버전", example = "rules-0.1")
    private String modelVersion;

    @Schema(description = "유효 만료(미리보기 캐시 등)", example = "2025-09-16T23:59:59")
    private LocalDateTime expiresAt;
}
