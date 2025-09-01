package com.jemyeonso.app.jemyeonsobe.api.ranking.service;

import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyRankingResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyScoreResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.repository.RankingRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final RankingRepository rankingRepository;

    public WeeklyRankingResponseDto getWeeklyRanking(int limit) {
        // User 테이블의 totalScore를 기준으로 랭킹 조회
        Pageable pageable = PageRequest.of(0, limit, Sort.by("totalScore").descending());
        List<User> topUsers = rankingRepository.findTopUsersByTotalScore(pageable);

        List<WeeklyRankingResponseDto.UserRankingDto> rankings = IntStream.range(0, topUsers.size())
                .mapToObj(i -> {
                    User user = topUsers.get(i);
                    return WeeklyRankingResponseDto.UserRankingDto.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .nickname(user.getNickname())
                            .profileImgUrl(user.getProfileImgUrl())
                            .totalScore(user.getTotalScore())
                            .rank(i + 1)
                            .build();
                })
                .toList();

        return WeeklyRankingResponseDto.builder()
                .rankings(rankings)
                .totalUsers(rankings.size())
                .build();
    }

    public WeeklyScoreResponseDto getWeeklyScore(Long userId) {
        // 사용자 존재 여부 확인
        User user = rankingRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        // 현재 사용자의 랭킹 조회
        Integer rank = rankingRepository.findUserRankByTotalScore(userId);

        return WeeklyScoreResponseDto.builder()
                .userId(user.getId())
                .totalScore(user.getTotalScore())
                .rank(rank != null ? rank : 0)
                .build();
    }
}
