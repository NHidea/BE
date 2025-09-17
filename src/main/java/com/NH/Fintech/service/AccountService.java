package com.NH.Fintech.service;

import com.NH.Fintech.domain.account.Account;
import com.NH.Fintech.domain.user.User;
import com.NH.Fintech.repository.AccountRepository;
import com.NH.Fintech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /** 계좌 생성 */
    @Transactional
    public Account create(Long userId, String bankName, String accountNumber, BigDecimal balance) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Account acc = Account.builder()
                .user(user)
                .bankName(bankName)
                .accountNumber(accountNumber)
                .balance(balance)
                .build();

        return accountRepository.save(acc);
    }

    /** 사용자별 계좌 조회 */
    public List<Account> findByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }
}
