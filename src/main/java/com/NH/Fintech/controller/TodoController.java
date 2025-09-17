package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.AddTodoItemRequest;
import com.NH.Fintech.controller.dto.ErrorResponse;
import com.NH.Fintech.controller.dto.TodoItemResponse;
import com.NH.Fintech.domain.todo.TodoItem;
import com.NH.Fintech.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Todo", description = "하루 단위 투두 리스트 API")
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "오늘(또는 지정일) 리스트 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/items/by-user/{userId}")
    public ResponseEntity<List<TodoItemResponse>> itemsForDate(
            @PathVariable Long userId,
            @RequestParam(value = "date", required = false) LocalDate date // 없으면 오늘
    ) {
        return ResponseEntity.ok(
                todoService.getItemsForDate(userId, date).stream()
                        .map(i -> TodoItemResponse.builder()
                                .itemId(i.getId())
                                .title(i.getTitle())
                                .summary(i.getSummary())
                                .checked(Boolean.TRUE.equals(i.getIsChecked()))
                                .orderIndex(i.getOrderIndex())
                                .build())
                        .toList()
        );
    }

    @Operation(summary = "리스트 추가(오늘 기본)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/item")
    public ResponseEntity<TodoItemResponse> addItem(@RequestBody AddTodoItemRequest req) {
        TodoItem item = todoService.addItem(
                req.getUserId(),
                req.getTitle(),
                req.getSummary(),
                req.getOrderIndex(),
                req.getPeriodDate() // null이면 오늘
        );

        return ResponseEntity.ok(
                TodoItemResponse.builder()
                        .itemId(item.getId())
                        .title(item.getTitle())
                        .summary(item.getSummary())
                        .checked(Boolean.TRUE.equals(item.getIsChecked()))
                        .orderIndex(item.getOrderIndex())
                        .build()
        );
    }

}
