package com.jemyeonso.app.jemyeonsobe.api.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WeeklyScoreResponseDto {
    private Long userId;
    private Integer totalScore;
    private Integer rank;
}
