package com.pado.domain.reflection.repository;

import com.pado.domain.reflection.entity.Reflection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ReflectionRepository extends JpaRepository<Reflection, Long> {

    List<Reflection> findByStudyId(Long studyId);

    interface UserReflectionCount{
        Long getStudyMemberId();
        Long getCnt();
    }

    @Query("""
        select sm.id as studyMemberId, count(r) as cnt 
        from Reflection r
        join r.studyMember sm
        where r.study.id = :studyId 
        group by sm.id         
    """)
    List<UserReflectionCount> countByStudyGroupByStudyMember(@Param("studyId") Long studyId);

    default Map<Long, Long> countMapByStudy(Long studyId){
        return countByStudyGroupByStudyMember(studyId).stream()
                .collect(Collectors.toMap(UserReflectionCount::getStudyMemberId, UserReflectionCount::getCnt));
    }
}
