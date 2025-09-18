package com.pado.domain.material.repository;

import com.pado.domain.dashboard.dto.LatestNoticeDto;
import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MaterialRepositoryCustom {
    Page<Material> findByStudyIdWithFiltersAndKeyword(
            Long studyId,
            List<MaterialCategory> categories,
            List<String> weeks,
            String keyword,
            Pageable pageable
    );

    Optional<LatestNoticeDto> findRecentNoticeAsDto(Long studyId, MaterialCategory category);
}
