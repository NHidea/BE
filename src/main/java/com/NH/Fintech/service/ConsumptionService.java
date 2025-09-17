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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsumptionService {

    private final ConsumptionLogRepository consumptionLogRepository;
    private final TodoItemRepository todoItemRepository;

    /** 소비 기록 생성 + (오늘+미완료) 매칭으로 TodoItem 자동 체크/연결 */
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
        Optional<TodoItem> matched = findFirstMatch(userId, content);

        matched.ifPresent(item -> {
            if (!Boolean.TRUE.equals(item.getIsChecked())) {
                item.setIsChecked(true);
                item.setCheckedAt(LocalDateTime.now());
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
     * 간단 매칭 규칙:
     * - 후보: 해당 사용자(userId)의 "오늘" + "미완료" 아이템
     * - 기준: 아이템 title/summary를 공백으로 토큰화 후, 2글자 이상 토큰이 소비 내용에 포함되면 매칭
     * - 먼저 찾은 한 건 반환
     */
    private Optional<TodoItem> findFirstMatch(Long userId, String logContent) {
        if (logContent == null || logContent.isBlank()) return Optional.empty();
        String lower = logContent.toLowerCase(Locale.ROOT);

        LocalDate today = LocalDate.now();

        List<TodoItem> candidates =
                todoItemRepository.findByUserIdAndPeriodDateAndIsCheckedFalseOrderByOrderIndexAsc(userId, today);

        for (TodoItem item : candidates) {
            String title = item.getTitle() != null ? item.getTitle() : "";
            String summary = item.getSummary() != null ? item.getSummary() : "";
            String text = (title + " " + summary).trim().toLowerCase(Locale.ROOT);

            if (!text.isBlank()) {
                for (String token : text.split("\\s+")) {
                    if (token.length() >= 2 && lower.contains(token)) {
                        return Optional.of(item);
                    }
                }
                if (lower.contains(text)) {
                    return Optional.of(item);
                }
            }
        }
        return Optional.empty();
    }
}
