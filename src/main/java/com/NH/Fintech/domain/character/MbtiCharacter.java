package com.NH.Fintech.domain.character;

import com.NH.Fintech.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mbti_character", indexes = {
        @Index(name = "idx_mbti_character_mbti_code", columnList = "mbti_code"),
        @Index(name = "idx_mbti_character_character_code", columnList = "character_code")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MbtiCharacter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    @Column(name = "mbti_code", length = 4, nullable = false)
    private String mbtiCode;

    @Column(name = "character_code", length = 50, nullable = false)
    private String characterCode;

    @Column(name = "character_name", length = 50, nullable = false)
    private String characterName;

    @Column(name = "tagline", length = 100)
    private String tagline;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "image_main_url", length = 500)
    private String imageMainUrl;
}
