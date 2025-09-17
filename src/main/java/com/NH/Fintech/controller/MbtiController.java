package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.*;
import com.NH.Fintech.domain.character.MbtiCharacter;
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

import java.util.List;
import java.util.Optional;

@Tag(name = "MBTI", description = "MBTI 질문/선택/평가 API")
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
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "해당 questionId가 존재하지 않습니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
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

    @Operation(summary = "사용자 답변 저장")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = UserMbtiAnswerResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "message": "optionId 또는 optionCode 중 하나는 필수입니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "질문/선택지 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/answers")
    public ResponseEntity<UserMbtiAnswerResponse> saveAnswer(@RequestBody SaveMbtiAnswerRequest req) {
        UserMbtiAnswer ans = mbtiService.saveAnswer(
                req.getUserId(), req.getQuestionId(), req.getOptionId(), req.getOptionCode()
        );

        UserMbtiAnswerResponse resp = UserMbtiAnswerResponse.builder()
                .answerId(ans.getId())
                .questionId(req.getQuestionId())
                .optionId(req.getOptionId())
                .optionCode(ans.getOptionCode())
                .answeredAt(ans.getAnsweredAt())
                .build();

        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "사용자 MBTI 평가 및 캐릭터 배정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "평가 성공",
                    content = @Content(schema = @Schema(implementation = MbtiEvaluationResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 답변 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/evaluate/{userId}")
    public ResponseEntity<MbtiEvaluationResponse> evaluate(@PathVariable Long userId) {
        Optional<MbtiCharacter> ch = mbtiService.evaluateAndAssignCharacter(userId);

        MbtiEvaluationResponse resp = MbtiEvaluationResponse.builder()
                .assigned(ch.isPresent())
                .mbtiCode(ch.map(MbtiCharacter::getMbtiCode).orElse(null))
                .characterId(ch.map(MbtiCharacter::getId).orElse(null))
                .characterName(ch.map(MbtiCharacter::getCharacterName).orElse(null))
                .build();

        return ResponseEntity.ok(resp);
    }
}
