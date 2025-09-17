package com.NH.Fintech.domain.account;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import com.NH.Fintech.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "account",
        indexes = @Index(name = "idx_account_user", columnList = "user_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 30)
    private String accountNumber;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;
}
