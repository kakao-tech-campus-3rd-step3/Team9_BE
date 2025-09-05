package com.pado.domain.material.repository;

import com.pado.domain.material.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByMaterialId(Long materialId);
}
