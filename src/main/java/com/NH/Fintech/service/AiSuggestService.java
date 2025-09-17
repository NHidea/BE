package com.NH.Fintech.service;

import com.NH.Fintech.controller.dto.AiTodoSuggestion;
import com.NH.Fintech.controller.dto.SuggestPreviewResponse;
import com.NH.Fintech.domain.todo.TodoItem;
import com.NH.Fintech.domain.user.User;
import com.NH.Fintech.repository.TodoItemRepository;
import com.NH.Fintech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiSuggestService {

    private final TodoItemRepository todoItemRepository;
    private final UserRepository userRepository;

    /** 미리보기: DB 저장 없이 제안 3건 생성 */
    public SuggestPreviewResponse preview(Long userId, LocalDate date) {
        LocalDate d = (date != null) ? date : LocalDate.now();
        // (임시 규칙) 이미 존재하는 오늘 아이템 타이틀 수집 → 중복 안나게 추천
        Set<String> existingTitles = todoItemRepository
                .findByUserIdAndPeriodDateOrderByOrderIndexAsc(userId, d)
                .stream().map(TodoItem::getTitle).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<AiTodoSuggestion> suggestions = ruleBasedThree(userId, d, existingTitles);

        return SuggestPreviewResponse.builder()
                .suggestions(suggestions)
                .source("mbti+spending")
                .modelVersion("rules-0.1")
                .expiresAt(LocalDateTime.of(d, LocalTime.MAX))
                .build();
    }

    /** 커밋: 선택된 제안(최대 3개)을 실제 TodoItem으로 저장 */
    @Transactional
    public List<TodoItem> commit(Long userId, LocalDate date, String requestId, List<AiTodoSuggestion> suggests) {
        // 존재하는지 확인(사용자)
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        LocalDate d = (date != null) ? date : LocalDate.now();

        // orderIndex 기본값 보정
        int base = Optional.ofNullable(
                todoItemRepository.findTopByUserIdAndPeriodDateOrderByOrderIndexDesc(userId, d)
        ).map(it -> Optional.ofNullable(it.getOrderIndex()).orElse(0)).orElse(0);

        List<TodoItem> result = new ArrayList<>();
        int seq = 1;
        for (AiTodoSuggestion s : suggests.stream().limit(3).toList()) {
            String title = Objects.requireNonNullElse(s.getTitle(), "").trim();
            if (title.isEmpty()) continue; // title 필수

            // 같은 날짜 같은 제목 있으면 스킵(멱등성 보장)
            if (todoItemRepository.existsByUserIdAndPeriodDateAndTitle(userId, d, title)) {
                continue;
            }

            Integer idx = (s.getOrderIndex() != null) ? s.getOrderIndex() : (base + seq);
            TodoItem item = TodoItem.builder()
                    .user(User.builder().id(userId).build())
                    .title(title)
                    .summary(s.getSummary())
                    .periodDate(d)
                    .isChecked(false)
                    .orderIndex(idx)
                    .build();
            result.add(todoItemRepository.save(item));
            seq++;
        }
        return result;
    }

    /** 매우 단순한 규칙 기반 3건 생성 (MBTI/소비내역 반영용 훅 자리) */
    private List<AiTodoSuggestion> ruleBasedThree(Long userId, LocalDate date, Set<String> existingTitles) {
        // TODO: MBTI, 최근 소비내역 리드 → 프롬프트 빌드 → LLM 호출(or 룰) 후 정규화
        // 지금은 예시로 MBTI 타입에 상관없이 “소비 습관 교정” 성격의 가벼운 3가지 추천
        List<String[]> pool = List.of(
                new String[]{"점심 후 바로 가계부 기록", "오늘 결제 1건이라도 기록 남기기"},
                new String[]{"카드명세 5분 점검", "어제 쓴 항목 중 불필요 지출 체크"},
                new String[]{"커피 지출 줄이기", "대체 음료 시도(티백/아메리카노 소)"},
                new String[]{"저녁 장보기 리스트 작성", "내일 아침 식사 재료 중심"},
                new String[]{"교통비 최적화 확인", "자주 가는 경로 대체 확인하기"}
        );

        // 중복 피해서 3개 뽑기
        List<AiTodoSuggestion> out = new ArrayList<>();
        int order = 1;
        for (String[] cand : pool) {
            String title = cand[0];
            if (!existingTitles.contains(title)) {
                out.add(AiTodoSuggestion.builder()
                        .userId(userId)
                        .periodDate(date)
                        .title(title)
                        .summary(cand[1])
                        .orderIndex(order++)
                        .build());
            }
            if (out.size() == 3) break;
        }
        // 만약 3개 미만이면, 남는 건 임의 보충
        while (out.size() < 3) {
            out.add(AiTodoSuggestion.builder()
                    .userId(userId)
                    .periodDate(date)
                    .title("지출 카테고리 분류 정리")
                    .summary("카테고리 미지정 3건 표시/정리")
                    .orderIndex(order++)
                    .build());
        }
        return out;
    }
}
