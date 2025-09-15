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
public interface MaterialRepository extends JpaRepository<Material, Long>, MaterialRepositoryCustom {

    // 기본 조회 (@PageableDefault의 정렬 적용)
    Page<Material> findByStudyId(Long studyId, Pageable pageable);

    List<Material> findByIdIn(List<Long> ids);
}
