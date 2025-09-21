package com.NH.Fintech.service;

import com.NH.Fintech.domain.character.MbtiCharacter;
import com.NH.Fintech.domain.mbti.MbtiOption;
import com.NH.Fintech.domain.mbti.MbtiQuestion;
import com.NH.Fintech.domain.mbti.UserMbtiAnswer;
import com.NH.Fintech.domain.user.User;
import com.NH.Fintech.repository.*;
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

    /** 평가 결과 전달용 레코드 */
    public record EvalResult(boolean assigned, String mbtiCode, MbtiCharacter character) {}
    /** 결과 조회용(프레젠테이션 조립 전 단계) */
    public record ResultPayload(boolean assigned, String mbtiCode, MbtiCharacter character,
                                long answered, long total) {}

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

    /**
     * 개별 답변 저장.
     * 저장 직후, 모든 문항 완료되면 자동 평가+배정(assign=true).
     */
    @Transactional
    public UserMbtiAnswer saveAnswer(Long userId, Long questionId, Long optionId, String optionCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        MbtiQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        // optionId 또는 optionCode로 검증
        MbtiOption option;
        if (optionId != null) {
            option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("Option not found: " + optionId));
            if (option.getQuestion() == null || !option.getQuestion().getId().equals(questionId)) {
                throw new IllegalArgumentException("해당 optionId는 전달된 questionId의 선택지가 아닙니다.");
            }
        } else if (optionCode != null && !optionCode.isBlank()) {
            final String oc = optionCode.trim().toUpperCase(Locale.ROOT);
            option = optionRepository.findByQuestionId(questionId).stream()
                    .filter(o -> oc.equalsIgnoreCase(o.getOptionCode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("questionId에 해당 optionCode가 존재하지 않습니다."));
        } else {
            throw new IllegalArgumentException("optionId 또는 optionCode 중 하나는 필수입니다.");
        }

        // (userId, questionId) 업서트
        UserMbtiAnswer entity = answerRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseGet(() -> UserMbtiAnswer.builder()
                        .user(user)
                        .question(question)
                        .build());
        entity.setOption(option);
        entity.setOptionCode(option.getOptionCode()); // DB 표준 코드로 저장
        entity.setAnsweredAt(LocalDateTime.now());

        UserMbtiAnswer saved = answerRepository.save(entity);

        // 자동 완료 감지 → 자동 평가+배정
        long totalQuestions = questionRepository.count();
        long answeredQuestions = answerRepository.countDistinctQuestionByUserId(userId);
        if (totalQuestions > 0 && answeredQuestions >= totalQuestions) {
            evaluate(userId, true);
        }
        return saved;
    }

    /** 저장된 답변으로 MBTI 계산. assign=true면 User.character 배정(멱등). */
    @Transactional
    public EvalResult evaluate(Long userId, boolean assign) {
        List<UserMbtiAnswer> answers = answerRepository.findByUserId(userId);
        if (answers.isEmpty()) return new EvalResult(false, null, null);

        String mbti = computeMbtiFromAnswers(answers);
        if (mbti == null) return new EvalResult(false, null, null);

        MbtiCharacter character = characterRepository.findByMbtiCode(mbti).orElse(null);
        boolean assigned = false;

        if (assign && character != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            // 동일 배정이면 no-op
            if (user.getCharacter() == null || !Objects.equals(user.getCharacter().getId(), character.getId())) {
                user.setCharacter(character);
                userRepository.save(user);
            }
            assigned = true;
        }
        return new EvalResult(assigned, mbti, character);
    }

    /** 결과 조회(미배정이면 진행률만, autoAssign=true면서 완료 상태면 즉시 배정) */
    @Transactional
    public ResultPayload getResult(Long userId, boolean autoAssignIfComplete) {
        long total = questionRepository.count();
        long answered = answerRepository.countDistinctQuestionByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        MbtiCharacter ch = user.getCharacter();

        if (ch == null && autoAssignIfComplete && total > 0 && answered >= total) {
            EvalResult r = evaluate(userId, true);
            ch = r.character();
        }

        String code = ch != null ? ch.getMbtiCode() : null;
        boolean assigned = ch != null;

        return new ResultPayload(assigned, code, ch, answered, total);
    }

    /* =========================================================
       가중치 기반 MBTI 산출 (Q1~Q4 × A~D 의미 반영)
       - option_code -> {I,E,N,S,F,T,P,J} 점수 테이블
       - 동점 시 최근 답변 1개에 소량 보정(+1) 적용
       ========================================================= */
    String computeMbtiFromAnswers(List<UserMbtiAnswer> answers) {
        Map<String, Map<String, Integer>> W = buildWeights();

        Map<String, Integer> score = new HashMap<>();
        for (String k : List.of("I","E","N","S","F","T","P","J")) score.put(k, 0);

        for (UserMbtiAnswer a : answers) {
            String oc = Optional.ofNullable(a.getOptionCode()).orElse("").trim();
            Map<String, Integer> w = W.get(oc);
            if (w != null) addScores(score, w);
        }

        // 동점 보정: 가장 최근 답변에 실제 가중된 축만 +1
        answers.stream()
                .filter(a -> a.getAnsweredAt() != null)
                .max(Comparator.comparing(UserMbtiAnswer::getAnsweredAt))
                .ifPresent(latest -> {
                    Map<String,Integer> lw = W.getOrDefault(latest.getOptionCode(), Collections.emptyMap());
                    lw.forEach((k,v) -> { if (v > 0) score.put(k, score.getOrDefault(k,0) + 1); });
                });

        boolean okIE = score.get("I") + score.get("E") > 0;
        boolean okNS = score.get("N") + score.get("S") > 0;
        boolean okFT = score.get("F") + score.get("T") > 0;
        boolean okPJ = score.get("P") + score.get("J") > 0;
        if (!(okIE && okNS && okFT && okPJ)) return null;

        String ie = (score.get("I") >= score.get("E")) ? "I" : "E";
        String ns = (score.get("N") >= score.get("S")) ? "N" : "S";
        String ft = (score.get("F") >= score.get("T")) ? "F" : "T";
        String pj = (score.get("P") >= score.get("J")) ? "P" : "J";
        return ie + ns + ft + pj;
    }

    /* 가중치 테이블: 화면 문구 의미를 반영한 설정 */
    private static Map<String, Map<String,Integer>> buildWeights() {
        Map<String, Map<String,Integer>> m = new HashMap<>();

        // Q1 월급(용돈) 받았을 때
        put(m,"Q1_A","J:3,S:2,T:1");         // 필수지출 정리
        put(m,"Q1_B","P:3,S:2");             // 갖고 싶은 것 하나
        put(m,"Q1_C","J:3,N:2,T:2");         // 저축/투자 계획
        put(m,"Q1_D","E:3,F:2,P:1,S:1");     // 가족/친구와 먹자

        // Q2 갑자기 10만원
        put(m,"Q2_A","J:3,S:2,I:1");         // 비상금 통장
        put(m,"Q2_B","P:3,S:2,F:1");         // 지금 사고 싶은 물건
        put(m,"Q2_C","N:3,T:2,P:1");         // 짧게 굴려 불리기
        put(m,"Q2_D","E:3,F:2,P:2,S:1");     // 맛집/모임

        // Q3 적금 선택 기준
        put(m,"Q3_A","T:4,S:2,J:1");         // 금리 최우선
        put(m,"Q3_B","P:4,S:2");             // 자유 입출
        put(m,"Q3_C","P:2,E:2,N:1");         // 이벤트/혜택
        put(m,"Q3_D","J:4,I:2,S:1");         // 작은 목표 끝까지

        // Q4 저축 챌린지 제안
        put(m,"Q4_A","J:4,T:2");             // 계획 세워 진행
        put(m,"Q4_B","E:4,F:2,J:1");         // 같이 하면 동기↑
        put(m,"Q4_C","I:4,J:2");             // 나만의 루틴(혼자)
        put(m,"Q4_D","P:4,I:2");             // 귀찮.. 안 함

        return m;
    }

    private static void put(Map<String, Map<String,Integer>> m, String code, String spec) {
        Map<String,Integer> w = new HashMap<>();
        for (String token : spec.split(",")) {
            String[] kv = token.trim().split(":");
            if (kv.length == 2) {
                w.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
            }
        }
        m.put(code, w);
    }

    private static void addScores(Map<String,Integer> score, Map<String,Integer> weight) {
        weight.forEach((k,v) -> score.put(k, score.getOrDefault(k, 0) + v));
    }
}
