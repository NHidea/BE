package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.*;
import com.NH.Fintech.domain.mbti.MbtiOption;
import com.NH.Fintech.domain.mbti.MbtiQuestion;
import com.NH.Fintech.domain.mbti.UserMbtiAnswer;
import com.NH.Fintech.service.MbtiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Tag(name = "MBTI", description = "MBTI 질문/선택 API (답변 완료 시 자동 배정, 결과 조회 제공)")
@RestController
@RequestMapping("/api/mbti")
@RequiredArgsConstructor
public class MbtiController {

    private final MbtiService mbtiService;

    @Operation(summary = "모든 MBTI 질문 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MbtiQuestionResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/questions")
    public ResponseEntity<List<MbtiQuestionResponse>> questions() {
        List<MbtiQuestion> questions = mbtiService.getQuestions();
        return ResponseEntity.ok(
                questions.stream()
                        .map(q -> MbtiQuestionResponse.builder()
                                .questionId(q.getId())
                                .questionCode(q.getQuestionCode())
                                .title(q.getTitle())
                                .content(q.getContent())
                                .displayOrder(q.getDisplayOrder())
                                .build())
                        .toList()
        );
    }

    @Operation(summary = "특정 질문의 선택지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MbtiOptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "질문 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/questions/{questionId}/options")
    public ResponseEntity<List<MbtiOptionResponse>> options(@PathVariable Long questionId) {
        List<MbtiOption> options = mbtiService.getOptionsByQuestion(questionId);
        return ResponseEntity.ok(
                options.stream()
                        .map(o -> MbtiOptionResponse.builder()
                                .optionId(o.getId())
                                .questionId(o.getQuestion() != null ? o.getQuestion().getId() : questionId)
                                .optionCode(o.getOptionCode())
                                .optionText(o.getOptionText())
                                .displayOrder(o.getDisplayOrder())
                                .build())
                        .toList()
        );
    }

    @Operation(summary = "사용자 답변 저장(개별 옵션 선택) — 모든 문항 완료 시 자동 MBTI 배정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = UserMbtiAnswerResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "질문/선택지 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/answers")
    public ResponseEntity<UserMbtiAnswerResponse> saveAnswer(@RequestBody SaveMbtiAnswerRequest req) {
        UserMbtiAnswer ans = mbtiService.saveAnswer(
                req.getUserId(), req.getQuestionId(), req.getOptionId(), req.getOptionCode()
        );

        UserMbtiAnswerResponse resp = UserMbtiAnswerResponse.builder()
                .answerId(ans.getId())
                .questionId(ans.getQuestion() != null ? ans.getQuestion().getId() : req.getQuestionId())
                .optionId(ans.getOption() != null ? ans.getOption().getId() : req.getOptionId())
                .optionCode(ans.getOptionCode())
                .answeredAt(ans.getAnsweredAt())
                .build();

        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "MBTI 결과 조회(배정된 경우 상세 반환, 미배정이면 진행률만)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MbtiResultResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/result/{userId}")
    public ResponseEntity<MbtiResultResponse> result(@PathVariable Long userId,
                                                     @RequestParam(name = "autoAssign", required = false, defaultValue = "true")
                                                     boolean autoAssign) {

        MbtiService.ResultPayload p = mbtiService.getResult(userId, autoAssign);

        // 배정된 경우: 캐릭터 메타(이미지/설명/특징) 안전 반출(리플렉션로드 – 필드명 달라도 컴파일 안전)
        String imageUrl = null;
        String description = null;
        List<String> traits = new ArrayList<>();

        if (p.character() != null) {
            Object ch = p.character();
            imageUrl = tryGetString(ch, "getImageMainUrl", "getMainImageUrl", "getImageUrl", "getProfileImageUrl");
            description = tryGetString(ch, "getDescription", "getSummary", "getIntro");

            // traits: List<String> 또는 String[] 또는 feature1~3
            Object traitList = tryGetObject(ch, "getTraits");
            if (traitList instanceof List<?> list) {
                list.stream().filter(Objects::nonNull).map(Object::toString).forEach(traits::add);
            } else if (traitList instanceof String[] arr) {
                for (String s : arr) traits.add(s);
            } else {
                // feature1..3 시도
                for (String m : new String[]{"getFeature1","getFeature2","getFeature3"}) {
                    String v = tryGetString(ch, m);
                    if (v != null && !v.isBlank()) traits.add(v);
                }
            }
        }

        MbtiResultResponse resp = MbtiResultResponse.builder()
                .assigned(p.assigned())
                .mbtiCode(p.mbtiCode())
                .characterId(p.character() != null ? p.character().getId() : null)
                .characterName(p.character() != null ? p.character().getCharacterName() : null)
                .imageUrl(imageUrl)
                .description(description)
                .traits(traits.isEmpty() ? null : traits)
                .answered(p.answered())
                .total(p.total())
                .build();

        return ResponseEntity.ok(resp);
    }

    /* --------- 리플렉션 헬퍼(엔티티 스키마 차이를 안전하게 흡수) --------- */
    private static String tryGetString(Object target, String... getters) {
        for (String g : getters) {
            try {
                Method m = target.getClass().getMethod(g);
                Object v = m.invoke(target);
                if (v != null) return v.toString();
            } catch (Exception ignore) {}
        }
        return null;
    }
    private static Object tryGetObject(Object target, String getter) {
        try {
            Method m = target.getClass().getMethod(getter);
            return m.invoke(target);
        } catch (Exception ignore) {
            return null;
        }
    }
}
