package com.pado.domain.basetime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 생성일과 수정일 모두 가지는 추상 클래스
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntity.class)
public abstract class AuditingEntity extends CreatedAtEntity {

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
