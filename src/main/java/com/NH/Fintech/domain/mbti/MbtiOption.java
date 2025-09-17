package com.NH.Fintech.domain.mbti;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mbti_option",
        uniqueConstraints = @UniqueConstraint(name = "uk_option_question_code",
                columnNames = {"question_id", "option_code"}),
        indexes = {
                @Index(name = "idx_mbti_option_question", columnList = "question_id"),
                @Index(name = "idx_mbti_option_code", columnList = "option_code")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MbtiOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private MbtiQuestion question;

    @Column(name = "option_code", length = 50, nullable = false)
    private String optionCode;

    @Column(name = "option_text", length = 500, nullable = false)
    private String optionText;

    @Column(name = "display_order")
    private Integer displayOrder;
}
