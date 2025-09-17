package com.jemyeonso.app.jemyeonsobe.api.ranking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class WeeklyRankingResponseDto {
    private List<UserRankingDto> rankings;
    private int totalUsers;
    private int currentPage;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    @Builder
    @Data
    public static class UserRankingDto {
        private Long userId;
        private String name;
        private String nickname;
        private String profileImgUrl;
        private Integer totalScore;
        private Integer rank;
    }
}
