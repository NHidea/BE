package com.NH.Fintech.service;

import com.NH.Fintech.domain.todo.TodoItem;
import com.NH.Fintech.domain.user.User;
import com.NH.Fintech.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoItemRepository todoItemRepository;

    /** 오늘(또는 지정 날짜)의 아이템 조회 */
    public List<TodoItem> getItemsForDate(Long userId, LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        return todoItemRepository.findByUserIdAndPeriodDateOrderByOrderIndexAsc(userId, target);
    }

    /** 오늘(또는 지정 날짜)의 미완료 아이템만 조회 (옵션) */
    public List<TodoItem> getUncheckedItemsForDate(Long userId, LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        return todoItemRepository.findByUserIdAndPeriodDateAndIsCheckedFalseOrderByOrderIndexAsc(userId, target);
    }

    /** 아이템 추가 (orderIndex 미지정 시 오늘 기준 자동 증가) */
    @Transactional
    public TodoItem addItem(Long userId, String title, String summary, Integer orderIndex, LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now();

        Integer idx = orderIndex;
        if (idx == null) {
            TodoItem last = todoItemRepository.findTopByUserIdAndPeriodDateOrderByOrderIndexDesc(userId, target);
            idx = (last != null && last.getOrderIndex() != null) ? last.getOrderIndex() + 1 : 1;
        }

        TodoItem item = TodoItem.builder()
                .user(User.builder().id(userId).build())
                .title(title)
                .summary(summary)
                .periodDate(target)
                .orderIndex(idx)
                .isChecked(false)
                .build();

        return todoItemRepository.save(item);
    }

    /** 체크/해제 */
    @Transactional
    public void setChecked(Long itemId, boolean checked) {
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("TodoItem not found: " + itemId));

        if (checked) {
            if (!Boolean.TRUE.equals(item.getIsChecked())) {
                item.setIsChecked(true);
                item.setCheckedAt(LocalDateTime.now());
            }
        } else {
            if (!Boolean.FALSE.equals(item.getIsChecked())) {
                item.setIsChecked(false);
                item.setCheckedAt(null);
            }
        }
    }
}
