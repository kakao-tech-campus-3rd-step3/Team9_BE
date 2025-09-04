package com.pado.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  //JPA에서 사용
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)    //AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false, length = 200)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(length = 100)
    private String region;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public User(String email, String passwordHash, String nickname, String region, String profileImageUrl, Gender gender){
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.region = region;
        this.profileImageUrl = profileImageUrl;
        this.gender = gender;
    }


    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeRegion(String region) {
        this.region = region;
    }

    public void changeProfileImage(String url) {
        this.profileImageUrl = url;
    }

}
