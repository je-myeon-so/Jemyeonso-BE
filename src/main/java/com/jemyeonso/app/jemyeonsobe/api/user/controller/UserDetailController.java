package com.jemyeonso.app.jemyeonsobe.api.user.controller;

import com.jemyeonso.app.jemyeonsobe.api.user.dto.ImproveRefreshRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserFeedbackResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserOverviewRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserOverviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.service.UserDetailService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.common.exception.ApiResponse;
import com.jemyeonso.app.jemyeonsobe.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend/users")
@Tag(name = "Users", description = "사용자 관련 API입니다.")
public class UserDetailController {

    private final UserDetailService userDetailService;

    @PostMapping("/me/improvement/refresh")
    public ResponseEntity<?> refreshImprovement(@RequestBody ImproveRefreshRequestDto requestDto) {
        Long userId = SecurityUtil.getCurrentUserId();
        userDetailService.refreshImprovementFromAi(userId, requestDto.getInterviewId(), requestDto.getDocumentId(), requestDto.getJobType());

        return ResponseEntity.ok(
            ApiResponse.success(ApiResponseCode.USER_IMPROVEMENT_REFRESH_SUCCESS, "개선점 갱신에 성공하였습니다.", null));
    }

    @GetMapping("/me/improvement")
    @Operation(summary = "유저 개선점 조회", description = "현재 로그인한 사용자의 개선점을 조회합니다.")
    public ResponseEntity<?> getImprovement() {
        Long userId = SecurityUtil.getCurrentUserId();

        UserFeedbackResponseDto responseDto = userDetailService.getImprovement(userId);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.USER_IMPROVEMENT_GET_SUCCESS, "개선점 조회에 성공하였습니다.", responseDto));
    }

    @GetMapping("/me/overview")
    @Operation(summary = "유저 소개 조회", description = "현재 로그인한 사용자의 한줄소개를 조회합니다.")
    public ResponseEntity<?> getOverview() {
        Long userId = SecurityUtil.getCurrentUserId();

        UserOverviewResponseDto responseDto = userDetailService.getOverview(userId);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.USER_OVERVIEW_GET_SUCCESS,"한줄 소개 조회에 성공하였습니다.", responseDto));
    }

    @PatchMapping("/me/overview")
    @Operation(summary = "유저 소개 수정", description = "현재 로그인한 사용자의 한줄소개를 수정합니다.")
    public ResponseEntity<?> patchOverview(
        @Parameter(description = "수정할 유저 정보 (한줄소개)")
        @RequestBody UserOverviewRequestDto requestDto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        String overview = requestDto.getOverview();
        UserOverviewResponseDto responseDto = userDetailService.patchOverview(userId, overview);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.USER_OVERVIEW_PATCH_SUCCESS, "유저 한줄 소개 수정에 성공하였습니다.", responseDto));
    }
}
