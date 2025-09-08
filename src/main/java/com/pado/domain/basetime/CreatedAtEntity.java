package com.pado.domain.basetime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 생성일만 가지는 추상 클래스
 */
@Getter
@MappedSuperclass
@EntityListeners(AutoCloseable.class)
public abstract class CreatedAtEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
