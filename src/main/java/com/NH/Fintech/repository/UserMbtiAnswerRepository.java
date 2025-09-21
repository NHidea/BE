package com.NH.Fintech.repository;

import com.NH.Fintech.domain.mbti.UserMbtiAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMbtiAnswerRepository extends JpaRepository<UserMbtiAnswer, Long> {

    List<UserMbtiAnswer> findByUserId(Long userId);

    Optional<UserMbtiAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);

    /** 사용자가 답변한(중복 제외) 질문 개수 */
    @Query("""
           select count(distinct a.question.id)
           from UserMbtiAnswer a
           where a.user.id = :userId
           """)
    long countDistinctQuestionByUserId(@Param("userId") Long userId);
}
