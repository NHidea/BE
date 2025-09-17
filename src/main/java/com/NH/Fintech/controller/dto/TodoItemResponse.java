package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "투두 리스트 응답 DTO")
public class TodoItemResponse {

    @Schema(description = "리스트 ID", example = "701")
    private Long itemId;

    @Schema(description = "제목", example = "아침에 물 2잔 마시기")
    private String title;

    @Schema(description = "요약(선택)", example = "기상 직후 작은 컵 기준 2잔")
    private String summary;

    @Schema(description = "체크 여부", example = "false")
    private boolean checked;

    @Schema(description = "정렬 순서", example = "1")
    private Integer orderIndex;
}