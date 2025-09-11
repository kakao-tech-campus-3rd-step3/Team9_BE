package com.pado.domain.user.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  //JPA에서 사용
@Table(name = "users")
public class User extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)    //AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(nullable = false, length = 200)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Region region;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInterest> interests = new ArrayList<>();

    @Builder
    public User(String email, String passwordHash, String nickname, Region region, String profileImageUrl, Gender gender){
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.region = region;
        this.profileImageUrl = profileImageUrl;
        this.gender = gender;
    }

    public void addInterest(Category category){
        if (category == null) throw new IllegalArgumentException("category는 필수입니다.");
        boolean exists = this.interests.stream()
                .anyMatch(ui -> ui.getCategory() == category);
        if (exists) throw new BusinessException(ErrorCode.DUPLICATE_INTEREST);
        UserInterest interest = UserInterest.builder()
                .user(this)
                .category(category)
                .build();
        this.interests.add(interest);
    }


    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeRegion(Region region) {
        this.region = region;
    }

    public void changeProfileImage(String url) {
        this.profileImageUrl = url;
    }

}
