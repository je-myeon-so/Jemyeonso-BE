package com.jemyeonso.app.jemyeonsobe.api.ranking.service;

import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyRankingResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyScoreResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.repository.RankingRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.UserDetail;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserDetailRepository;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserDetailRepository userDetailRepository;

    public WeeklyRankingResponseDto getWeeklyRanking(int limit) {
        log.info("주간 랭킹 조회 시작 - limit: {}", limit);

        try {
            // 조건에 맞는 모든 사용자 조회 (정렬됨)
            List<User> allUsers = rankingRepository.findTopUsersByTotalScore();
            log.info("조회된 사용자 수: {}", allUsers.size());

            // limit만큼만 자르기
            List<User> topUsers = allUsers.stream()
                    .limit(limit)
                    .toList();

            // DTO 변환
            List<WeeklyRankingResponseDto.UserRankingDto> rankings = IntStream.range(0, topUsers.size())
                    .mapToObj(i -> {
                        User user = topUsers.get(i);

                        // UserDetail에서 totalScore 가져오기
                        Integer totalScore = 0;
                        if (user.getUserDetail() != null) {
                            totalScore = user.getUserDetail().getTotalScore();
                        }

                        return WeeklyRankingResponseDto.UserRankingDto.builder()
                                .userId(user.getId())
                                .name(user.getName())
                                .nickname(user.getNickname())
                                .profileImgUrl(user.getProfileImgUrl())
                                .totalScore(totalScore)
                                .rank(i + 1)
                                .build();
                    })
                    .toList();

            log.info("주간 랭킹 조회 완료 - 반환된 랭킹 수: {}", rankings.size());

            return WeeklyRankingResponseDto.builder()
                    .rankings(rankings)
                    .totalUsers(rankings.size())
                    .build();

        } catch (Exception e) {
            log.error("주간 랭킹 조회 중 오류 발생", e);
            throw e;
        }
    }

    public WeeklyScoreResponseDto getWeeklyScore(Long userId) {
        log.info("사용자 주간 점수 조회 - userId: {}", userId);

        try {
            // 사용자 존재 여부 확인
            User user = rankingRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

            // UserDetail에서 totalScore 가져오기
            UserDetail userDetail = userDetailRepository.findByUserId(userId)
                    .orElseGet(() -> UserDetail.builder()
                            .user(user)
                            .userId(userId)
                            .totalScore(0)
                            .build());

            // 현재 사용자의 랭킹 조회
            Integer rank = rankingRepository.findUserRankByTotalScore(userId);

            log.info("사용자 주간 점수 조회 완료 - totalScore: {}, rank: {}",
                    userDetail.getTotalScore(), rank);

            return WeeklyScoreResponseDto.builder()
                    .userId(user.getId())
                    .totalScore(userDetail.getTotalScore())
                    .build();

        } catch (Exception e) {
            log.error("사용자 주간 점수 조회 중 오류 발생 - userId: {}", userId, e);
            throw e;
        }
    }
}
