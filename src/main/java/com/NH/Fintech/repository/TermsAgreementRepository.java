package com.NH.Fintech.repository;

import com.NH.Fintech.domain.terms.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {
    List<TermsAgreement> findByUserId(Long userId);
    Optional<TermsAgreement> findByUserIdAndTermsCode(Long userId, String termsCode);
}
