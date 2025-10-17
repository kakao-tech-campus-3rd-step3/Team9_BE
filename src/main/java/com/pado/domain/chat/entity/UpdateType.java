package com.pado.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UpdateType {
    IMOJI("imoji"),
    DELETED("deleted");

    private final String value;

    UpdateType(String value) {
        this.value = value;
    }

    @JsonValue  // JSON 직렬화 시 이 값을 사용
    public String getValue() {
        return value;
    }
}
