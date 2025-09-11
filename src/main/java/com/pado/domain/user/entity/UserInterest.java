package com.pado.domain.user.entity;

import com.pado.domain.shared.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_interests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_category", columnNames = {"user_id", "category"}
        )
)
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Builder
    private UserInterest(User user, Category category) {
        if (user == null) throw new IllegalArgumentException("user는 필수입니다.");
        if (category == null) throw new IllegalArgumentException("category는 필수입니다.");
        this.user = user;
        this.category = category;
    }
}

