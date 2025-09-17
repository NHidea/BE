package com.NH.Fintech.repository;

import com.NH.Fintech.domain.mbti.UserMbtiAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMbtiAnswerRepository extends JpaRepository<UserMbtiAnswer, Long> {
    List<UserMbtiAnswer> findByUserId(Long userId);
    Optional<UserMbtiAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);
}
