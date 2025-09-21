package com.NH.Fintech.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "MBTI 결과 조회 응답")
public class MbtiResultResponse {

    @Schema(description = "배정 여부", example = "true")
    private boolean assigned;

    @Schema(description = "MBTI 코드", example = "INFJ")
    private String mbtiCode;

    @Schema(description = "캐릭터 ID", example = "2001")
    private Long characterId;

    @Schema(description = "캐릭터 이름", example = "꿈꾸는 플래너, 올리")
    private String characterName;

    @Schema(description = "대표 이미지 URL")
    private String imageUrl;

    @Schema(description = "캐릭터 설명(요약)")
    private String description;

    @Schema(description = "특징 리스트")
    private List<String> traits;

    @Schema(description = "사용자가 답한 문항 수", example = "4")
    private Long answered;

    @Schema(description = "총 문항 수", example = "4")
    private Long total;
}
