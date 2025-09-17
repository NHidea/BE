package com.NH.Fintech.service;

import com.NH.Fintech.domain.character.MbtiCharacter;
import com.NH.Fintech.domain.mbti.MbtiOption;
import com.NH.Fintech.domain.mbti.MbtiQuestion;
import com.NH.Fintech.domain.mbti.UserMbtiAnswer;
import com.NH.Fintech.domain.user.User;
import com.NH.Fintech.repository.MbtiCharacterRepository;
import com.NH.Fintech.repository.MbtiOptionRepository;
import com.NH.Fintech.repository.MbtiQuestionRepository;
import com.NH.Fintech.repository.UserMbtiAnswerRepository;
import com.NH.Fintech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MbtiService {

    private final MbtiQuestionRepository questionRepository;
    private final MbtiOptionRepository optionRepository;
    private final UserMbtiAnswerRepository answerRepository;
    private final MbtiCharacterRepository characterRepository;
    private final UserRepository userRepository;

    public List<MbtiQuestion> getQuestions() {
        return questionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(q -> Optional.ofNullable(q.getDisplayOrder()).orElse(0)))
                .toList();
    }

    public List<MbtiOption> getOptionsByQuestion(Long questionId) {
        return optionRepository.findByQuestionId(questionId)
                .stream()
                .sorted(Comparator.comparing(o -> Optional.ofNullable(o.getDisplayOrder()).orElse(0)))
                .toList();
    }

    @Transactional
    public UserMbtiAnswer saveAnswer(Long userId, Long questionId, Long optionId, String optionCode) {
        Optional<UserMbtiAnswer> existing = answerRepository.findByUserIdAndQuestionId(userId, questionId);
        UserMbtiAnswer entity = existing.orElseGet(() -> UserMbtiAnswer.builder()
                .user(User.builder().id(userId).build())
                .question(MbtiQuestion.builder().id(questionId).build())
                .build());
        entity.setOption(MbtiOption.builder().id(optionId).build());
        entity.setOptionCode(optionCode);
        entity.setAnsweredAt(LocalDateTime.now());
        return answerRepository.save(entity);
    }

    /**
     * 매우 단순한 예시 산출 로직:
     * - question_code 별로 선택된 option_code를 집계하여 사전에 정의한 룰에 매칭
     * - 여기서는 4문항/4가지 축을 가정하고, 각 축에서 특정 선택에 점수 1 가산
     * - 실제 사업 로직에 맞게 룰을 주입/리팩토링하세요.
     */
    @Transactional
    public Optional<MbtiCharacter> evaluateAndAssignCharacter(Long userId) {
        List<UserMbtiAnswer> answers = answerRepository.findByUserId(userId);
        if (answers.size() == 0) return Optional.empty();

        // 예시: 각 선택지 코드로 간단 가중치를 합산 → 최다 득표 MBTI 코드 선택
        Map<String, Integer> score = new HashMap<>();
        for (UserMbtiAnswer a : answers) {
            String code = Optional.ofNullable(a.getOptionCode()).orElse("");
            // 여기에 실제 룰을 반영하세요. (아래는 예시: 코드 접두/접미에 따라 임의 가중)
            // ex) Q1: 계획/저축 성향 → 'INTJ/ISTJ' 가중
            if (code.contains("PLAN") || code.contains("SAVE")) {
                bump(score, "INTJ");
                bump(score, "ISTJ");
            }
            // ex) Q2: 소비/사교 성향 → 'ENFP/ESFP'
            if (code.contains("BUY") || code.contains("SOCIAL")) {
                bump(score, "ENFP");
                bump(score, "ESFP");
            }
            // ex) Q3: 금리중시 → 'INTJ/ENTJ'
            if (code.contains("RATE")) {
                bump(score, "INTJ");
                bump(score, "ENTJ");
            }
            // ex) Q4: 같이/혼자/안함 … 등등 추가 룰
            if (code.contains("TEAM")) bump(score, "ENFJ");
            if (code.contains("ALONE")) bump(score, "ISTP");
            if (code.contains("SKIP")) bump(score, "INFP");
        }

        String mbti = score.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
        if (mbti == null) return Optional.empty();

        Optional<MbtiCharacter> character = characterRepository.findByMbtiCode(mbti);
        character.ifPresent(c -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            user.setCharacter(c); // User.character 매핑
            userRepository.save(user);
        });
        return character;
    }

    private void bump(Map<String, Integer> score, String key) {
        score.put(key, score.getOrDefault(key, 0) + 1);
    }
}
