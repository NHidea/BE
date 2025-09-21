package com.NH.Fintech.service;

import com.NH.Fintech.domain.consumption.ConsumptionLog;
import com.NH.Fintech.domain.todo.TodoItem;
import com.NH.Fintech.domain.user.User;
import com.NH.Fintech.repository.ConsumptionLogRepository;
import com.NH.Fintech.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsumptionService {

    private static final String RULE_CODE = "AUTO_MATCH_TOKEN_V1"; // ← 규칙 기반임을 명시

    private final ConsumptionLogRepository consumptionLogRepository;
    private final TodoItemRepository todoItemRepository;

    /**
     * 소비 기록 생성 + (오늘+미완료) 토큰 매칭으로 TodoItem 자동 체크/연결
     */
    @Transactional
    public ConsumptionLog record(Long userId, BigDecimal amount, String content, LocalDateTime at) {
        ConsumptionLog log = ConsumptionLog.builder()
                .user(User.builder().id(userId).build())
                .amount(amount)
                .content(content)
                .transactionAt(at != null ? at : LocalDateTime.now())
                .build();

        // 저장
        ConsumptionLog saved = consumptionLogRepository.save(log);

        // 오늘 + 미완료 아이템만 후보로 매칭
        Optional<MatchResult> matched = findFirstMatch(userId, content);

        matched.ifPresent(mr -> {
            TodoItem item = mr.item();
            if (!Boolean.TRUE.equals(item.getIsChecked())) {
                item.setIsChecked(true);
                item.setCheckedAt(LocalDateTime.now());
                // 자동 체크 메타 기록 (AI 아님 / 규칙 기반)
                item.setAutoChecked(true);
                item.setAutoCheckedAt(LocalDateTime.now());
                item.setRuleCode(RULE_CODE);
                item.setRuleParams(buildRuleParamsJson(mr)); // 근거 JSON 기록
            }
            // 소비 ↔ 투두 연결 (FK는 소비 쪽)
            saved.setSatisfiedTodoItem(item);
        });

        return saved;
    }

    public List<ConsumptionLog> findUserLogs(Long userId) {
        return consumptionLogRepository.findByUserId(userId);
    }

    public List<ConsumptionLog> findUserLogsInRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return consumptionLogRepository.findByUserIdAndTransactionAtBetween(userId, start, end);
    }

    /**
     * 간단 토큰 매칭 규칙:
     * - 후보: 해당 사용자(userId)의 "오늘" + "미완료" 아이템
     * - 기준: 아이템 title/summary를 공백으로 토큰화 후, 길이 2+ 토큰이 소비 내용에 포함되면 매칭
     * - 먼저 찾은 한 건 반환
     */
    private Optional<MatchResult> findFirstMatch(Long userId, String logContent) {
        if (logContent == null || logContent.isBlank()) return Optional.empty();
        final String lower = normalize(logContent);

        LocalDate today = LocalDate.now();
        List<TodoItem> candidates =
                todoItemRepository.findByUserIdAndPeriodDateAndIsCheckedFalseOrderByOrderIndexAsc(userId, today);

        for (TodoItem item : candidates) {
            String title = Optional.ofNullable(item.getTitle()).orElse("");
            String summary = Optional.ofNullable(item.getSummary()).orElse("");
            String text = (title + " " + summary).trim();

            if (!text.isBlank()) {
                String[] tokens = text.split("\\s+");
                for (String raw : tokens) {
                    String token = normalize(raw);
                    if (token.length() >= 2 && lower.contains(token)) {
                        return Optional.of(new MatchResult(item, token, false, text, lower));
                    }
                }
                // 토큰으로 못 찾았으면 통문장 포함도 한번 검사
                String normalizedFull = normalize(text);
                if (normalizedFull.length() >= 2 && lower.contains(normalizedFull)) {
                    return Optional.of(new MatchResult(item, normalizedFull, true, text, lower));
                }
            }
        }
        return Optional.empty();
    }

    /** 소문자/트림 정도의 간단 정규화 (필요시 자모 분해/특수문자 제거 로직 추가 가능) */
    private String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).trim();
    }

    /** 매칭 근거를 rule_params(JSON)로 직렬화 */
    private String buildRuleParamsJson(MatchResult mr) {
        // 아주 단순히 수동 JSON 생성 (라이브러리 없이)
        // {"matchedToken":"커피","matchedByFullText":false,"sourceTitleSummary":"...","logContent":"..."}
        return new StringBuilder(256)
                .append("{")
                .append("\"matchedToken\":\"").append(escapeJson(mr.matchedToken())).append("\",")
                .append("\"matchedByFullText\":").append(mr.matchedByFullText()).append(",")
                .append("\"sourceTitleSummary\":\"").append(escapeJson(mr.sourceText())).append("\",")
                .append("\"logContent\":\"").append(escapeJson(mr.normalizedLog())).append("\"")
                .append("}")
                .toString();
    }

    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** 매칭 결과 DTO(내부용) */
    private record MatchResult(
            TodoItem item,
            String matchedToken,
            boolean matchedByFullText,
            String sourceText,
            String normalizedLog
    ) {}
}
