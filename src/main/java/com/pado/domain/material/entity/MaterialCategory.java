package com.pado.domain.material.entity;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

import java.util.Arrays;

public enum MaterialCategory {
    NOTICE("공지"),
    LEARNING("학습자료"),
    ASSIGNMENT("과제");

    public final String name;

    MaterialCategory(String name) {
        this.name = name;
    }

    public static MaterialCategory fromString(String categoryStr) {
        return Arrays.stream(values())
                .filter(category -> category.name.equals(categoryStr))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_MATERIAL_CATEGORY));
    }
}
