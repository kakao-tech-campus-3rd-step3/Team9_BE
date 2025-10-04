package com.pado.domain.reflection.repository;

import com.pado.domain.reflection.repository.dto.UserReflectionCountDto;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ReflectionRepositoryCustom {
    List<UserReflectionCountDto> countByStudyGroupByStudyMember(Long studyId);

    default Map<Long, Long> countMapByStudy(Long studyId) {
        return countByStudyGroupByStudyMember(studyId).stream()
                .collect(Collectors.toMap(UserReflectionCountDto::studyMemberId, UserReflectionCountDto::cnt));
    }
}
