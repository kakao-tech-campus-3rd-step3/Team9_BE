package com.pado.domain.chat.entity;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

import java.util.Arrays;

public enum ReactionType {
    LIKE,
    DISLIKE;

    public static ReactionType fromString(String reaction) {
        return Arrays.stream(ReactionType.values())
                .filter(type -> type.name().equalsIgnoreCase(reaction))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REACTION));
    }
}
