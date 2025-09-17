package com.NH.Fintech.domain.consumption;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import com.NH.Fintech.domain.todo.TodoItem;
import com.NH.Fintech.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "consumption_log",
        indexes = {
                @Index(name = "idx_cons_log_user", columnList = "user_id"),
                @Index(name = "idx_cons_log_tx_at", columnList = "transaction_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    /** 소유자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 거래 시각 */
    @Column(name = "transaction_at", nullable = false)
    private LocalDateTime transactionAt;

    /** 금액 */
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 거래 내용(간단 설명) */
    @Column(name = "content", length = 200)
    private String content;

    /** 이 소비로 충족된 투두 항목 (FK는 여기만 존재) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satisfied_todo_item_id")
    private TodoItem satisfiedTodoItem;
}
