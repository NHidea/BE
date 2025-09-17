package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.ConsumptionLogResponse;
import com.NH.Fintech.controller.dto.CreateConsumptionRequest;
import com.NH.Fintech.controller.dto.ErrorResponse;
import com.NH.Fintech.domain.consumption.ConsumptionLog;
import com.NH.Fintech.service.ConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Consumption", description = "소비 기록 API")
@RestController
@RequestMapping("/api/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @Operation(summary = "소비 기록 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ConsumptionLogResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "message": "amount는 0보다 커야 합니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ConsumptionLogResponse> create(@RequestBody CreateConsumptionRequest req) {
        LocalDateTime at = req.getTransactionAt() != null ? req.getTransactionAt() : LocalDateTime.now();
        ConsumptionLog log = consumptionService.record(req.getUserId(), req.getAmount(), req.getContent(), at);

        return ResponseEntity.ok(
                ConsumptionLogResponse.builder()
                        .logId(log.getId())
                        .userId(log.getUser().getId())
                        .amount(log.getAmount())
                        .content(log.getContent())
                        .transactionAt(log.getTransactionAt())
                        .satisfiedTodoItemId(log.getSatisfiedTodoItem() != null ? log.getSatisfiedTodoItem().getId() : null)
                        .build()
        );
    }

    @Operation(summary = "사용자별 소비 기록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ConsumptionLogResponse.class))),
            @ApiResponse(responseCode = "404", description = "기록 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "소비 기록이 없습니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<ConsumptionLogResponse>> userLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(
                consumptionService.findUserLogs(userId).stream()
                        .map(log -> ConsumptionLogResponse.builder()
                                .logId(log.getId())
                                .userId(log.getUser().getId())
                                .amount(log.getAmount())
                                .content(log.getContent())
                                .transactionAt(log.getTransactionAt())
                                .satisfiedTodoItemId(log.getSatisfiedTodoItem() != null ? log.getSatisfiedTodoItem().getId() : null)
                                .build())
                        .toList()
        );
    }

    @Operation(summary = "기간별 소비 기록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ConsumptionLogResponse.class))),
            @ApiResponse(responseCode = "400", description = "날짜 형식 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "message": "start/end 파라미터는 ISO_DATE_TIME 형식이어야 합니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-user/{userId}/range")
    public ResponseEntity<List<ConsumptionLogResponse>> userLogsInRange(
            @PathVariable Long userId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(
                consumptionService.findUserLogsInRange(userId, start, end).stream()
                        .map(log -> ConsumptionLogResponse.builder()
                                .logId(log.getId())
                                .userId(log.getUser().getId())
                                .amount(log.getAmount())
                                .content(log.getContent())
                                .transactionAt(log.getTransactionAt())
                                .satisfiedTodoItemId(log.getSatisfiedTodoItem() != null ? log.getSatisfiedTodoItem().getId() : null)
                                .build())
                        .toList()
        );
    }
}
