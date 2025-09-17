package com.NH.Fintech.domain.terms;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import com.NH.Fintech.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms_agreement",
        uniqueConstraints = @UniqueConstraint(name = "uk_terms_user_code",
                columnNames = {"user_id", "terms_code"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TermsAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "terms_code", length = 50, nullable = false)
    private String termsCode;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @Column(name = "is_agreed", nullable = false)
    private Boolean isAgreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;
}
