package com.NH.Fintech.domain.mbti;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import com.NH.Fintech.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_mbti_answer",
        uniqueConstraints = @UniqueConstraint(name = "uk_answer_user_question",
                columnNames = {"user_id", "question_id"}),
        indexes = {
                @Index(name = "idx_answer_user", columnList = "user_id"),
                @Index(name = "idx_answer_question", columnList = "question_id"),
                @Index(name = "idx_answer_option", columnList = "option_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserMbtiAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private MbtiQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private MbtiOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "option_code", length = 50)
    private String optionCode;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}
