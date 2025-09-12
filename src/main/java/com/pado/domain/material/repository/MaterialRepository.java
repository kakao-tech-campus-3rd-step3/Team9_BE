package com.pado.domain.material.repository;

import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // 기본 조회 (@PageableDefault의 정렬 적용)
    Page<Material> findByStudyId(Long studyId, Pageable pageable);
    
    // 통합 검색 (카테고리 + 주차 + 키워드) (Pageable의 정렬 설정 적용)
    @Query("SELECT m FROM Material m WHERE m.studyId = :studyId " +
            "AND (:categories IS NULL OR m.materialCategory IN :categories) " +
            "AND (:weeks IS NULL OR m.week IN :weeks OR (m.materialCategory != 'LEARNING' AND :weeks IS NOT NULL)) " +
            "AND (:keyword IS NULL OR " +
            "     m.title LIKE CONCAT('%', :keyword, '%') OR " +
            "     m.content LIKE CONCAT('%', :keyword, '%'))")
    Page<Material> findByStudyIdWithFiltersAndKeyword(
            @Param("studyId") Long studyId,
            @Param("categories") List<MaterialCategory> categories,
            @Param("weeks") List<String> weeks,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Material> findByIdIn(List<Long> ids);
    
    boolean existsByIdAndUserId(Long id, Long userId);
    
    boolean existsByIdAndStudyId(Long materialId, Long studyId);
}
