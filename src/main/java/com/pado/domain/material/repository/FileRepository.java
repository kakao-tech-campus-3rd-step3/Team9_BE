package com.pado.domain.material.repository;

import com.pado.domain.material.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByMaterialId(Long materialId);
    List<File> findByMaterialIdIn(List<Long> materialIds);

    void deleteByMaterialId(Long materialId);
    void deleteAllByUrlIn(List<String> urls);
}