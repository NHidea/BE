package com.NH.Fintech.domain.todo;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import com.NH.Fintech.domain.consumption.ConsumptionLog;
import com.NH.Fintech.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "todo_item",
        indexes = {
                @Index(name = "idx_todo_item_user", columnList = "user_id"),
                @Index(name = "idx_todo_item_checked", columnList = "is_checked"),
                @Index(name = "idx_todo_item_period_date", columnList = "period_date"),
                @Index(name = "idx_todo_item_order", columnList = "order_index")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_item_id")
    private Long id;

    /** 소유자 (리스트 제거 → 아이템이 사용자에 직접 소속) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 할 일 제목 (기존 content → title) */
    @Column(name = "title", length = 150, nullable = false)
    private String title;

    /** 할 일 세부 내용(옵션) */
    @Column(name = "summary", length = 1000)
    private String summary;

    /** 하루 단위 기간(예: 해야 할 날짜 / 마감일) */
    @Column(name = "period_date")
    private LocalDate periodDate;

    /** 완료 여부 */
    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    private Boolean isChecked = false;

    /** 완료 시각 */
    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    /** 정렬 순서 */
    @Column(name = "order_index")
    private Integer orderIndex;

    /** 이 항목을 충족시킨 소비 로그 (FK는 소비 테이블에 존재) */
    @OneToOne(mappedBy = "satisfiedTodoItem", fetch = FetchType.LAZY)
    private ConsumptionLog consumptionLog;
}
