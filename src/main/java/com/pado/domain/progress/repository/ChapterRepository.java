package com.pado.domain.progress.repository;

import com.pado.domain.progress.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByStudyId(Long studyId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Chapter c set c.content = :content where c.id = :id")
    int updateContent(@Param("id") Long id, @Param("content") String content);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Chapter c set c.completed = true where c.id = :id")
    int complete(@Param("id") Long id);
}
