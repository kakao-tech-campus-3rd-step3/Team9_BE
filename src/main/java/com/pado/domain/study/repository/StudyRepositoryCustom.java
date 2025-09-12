package com.pado.domain.study.repository;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.entity.Study;
import com.pado.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface StudyRepositoryCustom {
    Slice<Study> findStudiesByFilter(User user, String keyword, List<Category> categories, List<Region> regions, Pageable pageable);
}