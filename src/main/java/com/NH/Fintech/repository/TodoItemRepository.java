package com.NH.Fintech.repository;

import com.NH.Fintech.domain.todo.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    /** 오늘(혹은 지정일) 아이템 정렬순 */
    List<TodoItem> findByUserIdAndPeriodDateOrderByOrderIndexAsc(Long userId, LocalDate periodDate);

    /** 오늘 + 미완료만 */
    List<TodoItem> findByUserIdAndPeriodDateAndIsCheckedFalseOrderByOrderIndexAsc(Long userId, LocalDate periodDate);

    /** 지정일의 마지막 orderIndex */
    TodoItem findTopByUserIdAndPeriodDateOrderByOrderIndexDesc(Long userId, LocalDate periodDate);

    /** 동일 날짜에 같은 제목 존재 여부(중복 방지) */
    boolean existsByUserIdAndPeriodDateAndTitle(Long userId, LocalDate periodDate, String title);
}
