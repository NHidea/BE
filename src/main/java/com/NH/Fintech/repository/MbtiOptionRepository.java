package com.NH.Fintech.repository;

import com.NH.Fintech.domain.mbti.MbtiOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MbtiOptionRepository extends JpaRepository<MbtiOption, Long> {
    List<MbtiOption> findByQuestionId(Long questionId);
    Optional<MbtiOption> findByQuestionIdAndOptionCode(Long questionId, String optionCode);
}
