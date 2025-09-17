package com.NH.Fintech.repository;

import com.NH.Fintech.domain.mbti.MbtiQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MbtiQuestionRepository extends JpaRepository<MbtiQuestion, Long> {
    Optional<MbtiQuestion> findByQuestionCode(String code);
}
