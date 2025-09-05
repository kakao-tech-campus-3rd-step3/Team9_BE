package com.pado.domain.material.repository;

import com.pado.domain.material.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByStudyId(Long studyId);
}
