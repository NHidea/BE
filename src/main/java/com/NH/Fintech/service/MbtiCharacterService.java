package com.NH.Fintech.service;

import com.NH.Fintech.domain.character.MbtiCharacter;
import com.NH.Fintech.repository.MbtiCharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MbtiCharacterService {

    private final MbtiCharacterRepository repository;

    /**
     * MBTI 코드로 캐릭터 조회
     */
    public MbtiCharacter findByMbtiCode(String code) {
        return repository.findByMbtiCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 MBTI 코드: " + code));
    }

    /**
     * 모든 캐릭터 조회
     */
    public List<MbtiCharacter> findAll() {
        return repository.findAll();
    }

    /**
     * ID로 캐릭터 조회
     */
    public MbtiCharacter findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("캐릭터를 찾을 수 없습니다. id=" + id));
    }

    /**
     * 캐릭터 저장 (신규/수정 겸용)
     */
    @Transactional
    public MbtiCharacter save(MbtiCharacter character) {
        return repository.save(character);
    }

    /**
     * 캐릭터 삭제
     */
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 캐릭터가 존재하지 않습니다. id=" + id);
        }
        repository.deleteById(id);
    }
}
