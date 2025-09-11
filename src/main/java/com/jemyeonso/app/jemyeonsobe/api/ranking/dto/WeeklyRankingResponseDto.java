package com.jemyeonso.app.jemyeonsobe.api.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WeeklyRankingResponseDto {
    private List<UserRankingDto> rankings;
    private int totalUsers;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserRankingDto {
        private Long userId;
        private String name;
        private String nickname;
        private String profileImgUrl;
        private Integer totalScore;
        private Integer rank;
    }
}
