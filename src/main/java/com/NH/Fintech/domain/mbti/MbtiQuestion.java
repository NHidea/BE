package com.NH.Fintech.domain.mbti;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mbti_question",
        indexes = @Index(name = "idx_mbti_question_code", columnList = "question_code"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MbtiQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @Column(name = "question_code", length = 50, nullable = false)
    private String questionCode;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "content", length = 500)
    private String content;

    @Column(name = "display_order")
    private Integer displayOrder;
}
