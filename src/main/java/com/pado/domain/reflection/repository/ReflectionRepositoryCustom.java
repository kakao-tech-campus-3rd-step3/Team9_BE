package com.pado.domain.reflection.repository;

import com.pado.domain.reflection.entity.Reflection;
import com.pado.domain.reflection.repository.dto.UserReflectionCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ReflectionRepositoryCustom {

    List<UserReflectionCountDto> countByStudyGroupByStudyMember(Long studyId);

    default Map<Long, Long> countMapByStudy(Long studyId) {
        return countByStudyGroupByStudyMember(studyId).stream()
            .collect(Collectors.toMap(UserReflectionCountDto::studyMemberId,
                UserReflectionCountDto::cnt));
    }

    Page<Reflection> findReflectionsByStudy(Long studyId, Long userId, Pageable pageable);
}