package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CursorResponseDto<T extends CursorIdentifiable>(
        List<T> content,

        @JsonProperty("next_cursor")
        Long nextCursor,

        @JsonProperty("has_next")
        boolean hasNext
) {

}