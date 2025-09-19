package com.pado.domain.dashboard.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "최근 공지사항 정보")
public record LatestNoticeDto(
        @Schema(description = "공지사항 ID")
        Long notice_id,

        @Schema(description = "공지사항 제목")
        String title,

        @Schema(description = "작성자 이름")
        String author_name,

        @Schema(description = "작성 시각")
        LocalDateTime created_at
) {
        @QueryProjection
        public LatestNoticeDto(Long notice_id, String title, String author_name, LocalDateTime created_at) {
                this.notice_id = notice_id;
                this.title = title;
                this.author_name = author_name;
                this.created_at = created_at;
        }
}
