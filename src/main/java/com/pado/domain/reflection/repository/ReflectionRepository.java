package com.pado.domain.reflection.repository;

import com.pado.domain.reflection.entity.Reflection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {

    List<Reflection> findByStudyId(Long studyId);
}
