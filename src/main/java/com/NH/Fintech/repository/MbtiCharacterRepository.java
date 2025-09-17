package com.NH.Fintech.repository;

import com.NH.Fintech.domain.character.MbtiCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MbtiCharacterRepository extends JpaRepository<MbtiCharacter, Long> {
    Optional<MbtiCharacter> findByMbtiCode(String mbtiCode);
    Optional<MbtiCharacter> findByCharacterCode(String characterCode);
}
