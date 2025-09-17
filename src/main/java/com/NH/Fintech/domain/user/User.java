package com.NH.Fintech.domain.user;

import com.NH.Fintech.domain.character.MbtiCharacter;
import com.NH.Fintech.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private MbtiCharacter character;

    @Column(name = "account_holder_name", length = 50)
    private String accountHolderName;

    @Column(name = "institution_code", length = 20)
    private String institutionCode;

    @Column(name = "auth_key", length = 255)
    private String authKey;
}
