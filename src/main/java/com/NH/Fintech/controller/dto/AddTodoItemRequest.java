package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "투두 리스트 추가 요청 DTO (오늘 기본)")
public class AddTodoItemRequest {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "제목(필수)", example = "아침 30분 조깅하기")
    private String title;

    @Schema(description = "세부 내용(선택)", example = "동네 공원 트랙 3바퀴")
    private String summary;

    @Schema(description = "표시 순서(선택). 미지정 시 자동 증가", example = "3")
    private Integer orderIndex;

    @Schema(description = "리스트 날짜(선택). 미지정 시 오늘", example = "2025-09-16")
    private LocalDate periodDate; // null이면 서비스에서 LocalDate.now() 적용
}
