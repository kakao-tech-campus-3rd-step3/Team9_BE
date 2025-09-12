package com.pado.domain.shared.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum Category {
    LANGUAGE("어학"),
    EMPLOYMENT("취업"),
    EXAM("고시/공무원"),
    HOBBY("취미/교양"),
    PROGRAMMING("프로그래밍"),
    AUTONOMY("자율/기타");

    @JsonValue
    private final String krName;

    @JsonCreator
    public static Category from(String krName) {
        return Stream.of(Category.values())
                .filter(category -> category.getKrName().equals(krName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("interests_category: 존재하지 않는 카테고리입니다: " + krName));
    }
}