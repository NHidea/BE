package com.NH.Fintech.service;

import com.NH.Fintech.domain.terms.TermsAgreement;
import com.NH.Fintech.repository.TermsAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsAgreementService {

    private final TermsAgreementRepository termsAgreementRepository;

    public List<TermsAgreement> findByUser(Long userId) {
        return termsAgreementRepository.findByUserId(userId);
    }

    public Optional<TermsAgreement> findByUserAndCode(Long userId, String termsCode) {
        return termsAgreementRepository.findByUserIdAndTermsCode(userId, termsCode);
    }

    @Transactional
    public TermsAgreement agree(Long userId, String termsCode, boolean required, boolean agreed) {
        TermsAgreement entity = findByUserAndCode(userId, termsCode)
                .orElseGet(() -> TermsAgreement.builder()
                        .user(com.NH.Fintech.domain.user.User.builder().id(userId).build())
                        .termsCode(termsCode)
                        .isRequired(required)
                        .build());
        entity.setIsAgreed(agreed);
        entity.setAgreedAt(agreed ? LocalDateTime.now() : null);
        return termsAgreementRepository.save(entity);
    }
}
