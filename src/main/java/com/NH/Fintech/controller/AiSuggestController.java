package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.*;
import com.NH.Fintech.domain.todo.TodoItem;
import com.NH.Fintech.service.AiSuggestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "AI-Todo", description = "AI 투두 제안(미리보기/커밋)")
@RestController
@RequestMapping("/api/ai/todos")
@RequiredArgsConstructor
public class AiSuggestController {

    private final AiSuggestService aiSuggestService;

    @Operation(summary = "하루 3개 투두 제안 미리보기(비저장)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = SuggestPreviewResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/preview")
    public ResponseEntity<SuggestPreviewResponse> preview(
            @RequestParam Long userId,
            @RequestParam(required = false) LocalDate date
    ) {
        return ResponseEntity.ok(aiSuggestService.preview(userId, date));
    }

    @Operation(summary = "미리보기 중 선택된 제안을 실제 투두로 커밋(저장)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성됨",
                    content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/commit")
    public ResponseEntity<List<TodoItemResponse>> commit(@RequestBody SuggestCommitRequest req) {
        List<TodoItem> saved = aiSuggestService.commit(
                req.getUserId(), req.getPeriodDate(), req.getRequestId(), req.getSuggestions()
        );
        return ResponseEntity.ok(
                saved.stream().map(i -> TodoItemResponse.builder()
                        .itemId(i.getId())
                        .title(i.getTitle())
                        .summary(i.getSummary())
                        .checked(Boolean.TRUE.equals(i.getIsChecked()))
                        .orderIndex(i.getOrderIndex())
                        .build()
                ).toList()
        );
    }
}
