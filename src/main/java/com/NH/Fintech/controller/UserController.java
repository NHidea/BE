package com.NH.Fintech.controller;

import com.NH.Fintech.controller.dto.ErrorResponse;
import com.NH.Fintech.domain.user.User;
import com.NH.Fintech.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.ok(userService.save(user));
    }

    @Operation(summary = "사용자 단건 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "사용자를 찾을 수 없습니다.",
                                      "timestamp": "2025-09-16T12:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        Optional<User> u = userService.findById(id);
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
