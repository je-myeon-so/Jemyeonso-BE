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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public WeeklyRankingResponseDto getWeeklyRanking(int page, int size) {
        log.info("주간 랭킹 조회 시작 - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = rankingRepository.findTopUsersByTotalScore(pageable);
            List<User> topUsers = userPage.getContent();

            log.info("조회된 사용자 수: {}, 전체 페이지: {}, 현재 페이지: {}",
                    topUsers.size(), userPage.getTotalPages(), page);

            // 실제 랭킹 계산 (페이지 offset 고려)
            int rankOffset = page * size;
            List<WeeklyRankingResponseDto.UserRankingDto> rankings = IntStream.range(0, topUsers.size())
                    .mapToObj(i -> {
                        User user = topUsers.get(i);

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
                                .rank(rankOffset + i + 1) // 페이지 offset 적용
                                .build();
                    })
                    .toList();

            log.info("주간 랭킹 조회 완료 - 반환된 랭킹 수: {}", rankings.size());

            return WeeklyRankingResponseDto.builder()
                    .rankings(rankings)
                    .totalUsers((int) userPage.getTotalElements())
                    .currentPage(page)
                    .totalPages(userPage.getTotalPages())
                    .hasNext(userPage.hasNext())
                    .hasPrevious(userPage.hasPrevious())
                    .build();

        } catch (Exception e) {
            log.error("주간 랭킹 조회 중 오류 발생", e);
            throw e;
        }
    }

    public WeeklyScoreResponseDto getWeeklyScore(Long userId) {
        log.info("사용자 주간 점수 조회 - userId: {}", userId);

        try {
            User user = rankingRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

            UserDetail userDetail = userDetailRepository.findByUserId(userId)
                    .orElseGet(() -> UserDetail.builder()
                            .user(user)
                            .userId(userId)
                            .totalScore(0)
                            .build());

            Integer rank = rankingRepository.findUserRankByTotalScore(userId);

            log.info("사용자 주간 점수 조회 완료 - totalScore: {}, rank: {}",
                    userDetail.getTotalScore(), rank);

            return WeeklyScoreResponseDto.builder()
                    .userId(user.getId())
                    .totalScore(userDetail.getTotalScore())
                    .rank(rank)
                    .build();

        } catch (Exception e) {
            log.error("사용자 주간 점수 조회 중 오류 발생 - userId: {}", userId, e);
            throw e;
        }
    }
}
