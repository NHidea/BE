package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.AgreeTermsRequest;
import com.NH.Fintech.controller.dto.ErrorResponse;
import com.NH.Fintech.controller.dto.TermsAgreementResponse;
import com.NH.Fintech.domain.terms.TermsAgreement;
import com.NH.Fintech.service.TermsAgreementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Terms", description = "이용약관 동의 API")
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsAgreementService termsService;

    @Operation(summary = "사용자별 약관동의 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TermsAgreementResponse.class))),
            @ApiResponse(responseCode = "404", description = "동의 기록 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<TermsAgreementResponse>> byUser(@PathVariable Long userId) {
        List<TermsAgreement> list = termsService.findByUser(userId);
        return ResponseEntity.ok(
                list.stream().map(a -> TermsAgreementResponse.builder()
                        .agreementId(a.getId())
                        .userId(a.getUser().getId())
                        .termsCode(a.getTermsCode())
                        .required(a.getIsRequired())
                        .agreed(a.getIsAgreed())
                        .agreedAt(a.getAgreedAt())
                        .build()
                ).collect(Collectors.toList())
        );
    }

    @Operation(summary = "약관 동의/철회 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공",
                    content = @Content(schema = @Schema(implementation = TermsAgreementResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "message": "termsCode는 필수입니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/agree")
    public ResponseEntity<TermsAgreementResponse> agree(@RequestBody AgreeTermsRequest req) {
        TermsAgreement a = termsService.agree(req.getUserId(), req.getTermsCode(), req.isRequired(), req.isAgreed());
        TermsAgreementResponse resp = TermsAgreementResponse.builder()
                .agreementId(a.getId())
                .userId(a.getUser().getId())
                .termsCode(a.getTermsCode())
                .required(a.getIsRequired())
                .agreed(a.getIsAgreed())
                .agreedAt(a.getAgreedAt())
                .build();
        return ResponseEntity.ok(resp);
    }
}
