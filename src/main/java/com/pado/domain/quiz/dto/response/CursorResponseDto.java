package com.pado.domain.quiz.dto.response;

import java.util.List;

public record CursorResponseDto<T extends CursorIdentifiable>(
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {

}