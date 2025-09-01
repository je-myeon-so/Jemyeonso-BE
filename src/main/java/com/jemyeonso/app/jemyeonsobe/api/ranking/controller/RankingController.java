package com.jemyeonso.app.jemyeonsobe.api.ranking.controller;

import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyRankingResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.dto.WeeklyScoreResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.ranking.service.RankingService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponse;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backend/ranking")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "랭킹 및 점수 관련 API")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/weekly")
    @Operation(
            summary = "주간 면접 점수 랭킹 조회",
            description = "전체 유저의 최근 일주일간 면접 점수를 내림차순으로 조회합니다."
    )
    public ResponseEntity<?> getWeeklyRanking(
            @Parameter(description = "조회할 상위 순위 수 (기본값: 100, 최대: 1000)")
            @RequestParam(defaultValue = "100") int limit) {

        if (limit < 1 || limit > 1000) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ApiResponseCode.PAGINATION_INVALID_PARAMETER));
        }

        try {
            WeeklyRankingResponseDto response = rankingService.getWeeklyRanking(limit);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            ApiResponseCode.WEEKLY_RANKING_GET_SUCCESS,
                            "주간 랭킹 조회에 성공하였습니다.",
                            response
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR));
        }
    }

    @GetMapping("/my-score")
    @Operation(
            summary = "내 주간 면접 점수 조회",
            description = "현재 로그인한 사용자의 최근 일주일간 면접 점수 합계를 조회합니다."
    )
    public ResponseEntity<?> getMyWeeklyScore() {
        Long userId = SecurityUtil.getCurrentUserId();

        try {
            WeeklyScoreResponseDto response = rankingService.getWeeklyScore(userId);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            ApiResponseCode.WEEKLY_SCORE_GET_SUCCESS,
                            "주간 점수 조회에 성공하였습니다.",
                            response
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR));
        }
    }
}
