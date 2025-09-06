package com.pado.domain.material.repository;

import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Page<Material> findByStudyIdOrderByCreatedAtDesc(Long studyId, Pageable pageable);
    
    Page<Material> findByStudyIdAndMaterialCategoryInOrderByCreatedAtDesc(
            Long studyId, 
            List<MaterialCategory> categories, 
            Pageable pageable
    );
    
    List<Material> findByIdIn(List<Long> ids);
    
    boolean existsByIdAndUserId(Long id, Long userId);
    
    boolean existsByIdAndStudyId(Long materialId, Long studyId);
}
