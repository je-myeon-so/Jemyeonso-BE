package com.jemyeonso.app.jemyeonsobe.api.ranking.service;

import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyRankingResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyScoreResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.repository.RankingRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final RankingRepository rankingRepository;

    // totalscore 기준으로 랭킹 조회
    public WeeklyRankingResponseDto getWeeklyRanking(int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusWeeks(1);

        List<Object[]> userScores = rankingRepository.findWeeklyRankings(weekAgo, now, limit);

        List<WeeklyRankingResponseDto.UserRankingDto> rankings = new ArrayList<>();
        int rank = 1;

        for (Object[] result : userScores) {
            User user = (User) result[0];
            Long totalScore = (Long) result[1];
            Long interviewCount = (Long) result[2];
            Double averageScore = interviewCount > 0 ? (double) totalScore / interviewCount : 0.0;

            rankings.add(WeeklyRankingResponseDto.UserRankingDto.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .profileImgUrl(user.getProfileImgUrl())
                    .totalScore(totalScore.intValue())
                    .interviewCount(interviewCount.intValue())
                    .averageScore(Math.round(averageScore * 100.0) / 100.0)
                    .rank(rank++)
                    .build());
        }

        return WeeklyRankingResponseDto.builder()
                .rankings(rankings)
                .totalUsers(rankings.size())
                .build();
    }

    // 자신의 주간 점수 조회
    public WeeklyScoreResponseDto getWeeklyScore(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusWeeks(1);

        List<Object[]> userScore = rankingRepository.findWeeklyScoreByUserId(userId, weekAgo, now);

        if (userScore.isEmpty()) {
            return WeeklyScoreResponseDto.builder()
                    .userId(userId)
                    .totalScore(0)
                    .interviewCount(0)
                    .averageScore(0.0)
                    .build();
        }

        Object[] result = userScore.get(0);
        Long totalScore = (Long) result[0];
        Long interviewCount = (Long) result[1];
        Double averageScore = interviewCount > 0 ? (double) totalScore / interviewCount : 0.0;

        return WeeklyScoreResponseDto.builder()
                .userId(userId)
                .totalScore(totalScore.intValue())
                .interviewCount(interviewCount.intValue())
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .build();
    }
}
