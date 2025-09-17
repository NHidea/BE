package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.AccountResponse;
import com.NH.Fintech.controller.dto.CreateAccountRequest;
import com.NH.Fintech.controller.dto.ErrorResponse;
import com.NH.Fintech.domain.account.Account;
import com.NH.Fintech.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Account", description = "계좌 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계좌 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "message": "userId는 필수입니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "예상치 못한 오류가 발생했습니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """)))
    })
    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody CreateAccountRequest req) {
        Account acc = accountService.create(
                req.getUserId(), req.getBankName(), req.getAccountNumber(), req.getBalance()
        );
        return ResponseEntity.ok(
                AccountResponse.builder()
                        .accountId(acc.getId())
                        .userId(acc.getUser().getId())
                        .bankName(acc.getBankName())
                        .accountNumber(acc.getAccountNumber())
                        .balance(acc.getBalance())
                        .build()
        );
    }

    @Operation(summary = "사용자별 계좌 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 계좌 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "계좌가 없습니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<AccountResponse>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                accountService.findByUser(userId).stream()
                        .map(acc -> AccountResponse.builder()
                                .accountId(acc.getId())
                                .userId(acc.getUser().getId())
                                .bankName(acc.getBankName())
                                .accountNumber(acc.getAccountNumber())
                                .balance(acc.getBalance())
                                .build())
                        .toList()
        );
    }
}
