package com.jemyeonso.app.jemyeonsobe.api.user.controller;

import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserInfoRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserInfoResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.service.UserService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.common.exception.ApiResponse;
import com.jemyeonso.app.jemyeonsobe.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend/users")
@Tag(name = "Users", description = "사용자 관련 API입니다.")
public class UserController {

    private final UserService userService;

    /**
     * 유저 정보 조회 API
     * @return 성공응답
     */
    @GetMapping("/me")
    @Operation(summary = "유저 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<?> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();

        UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "유저 정보 조회에 성공하였습니다.", userInfoResponseDto));
    }

    /**
     * 유저 정보 수정 API
     * @param userInfoRequestDto (닉네임, 유저 프로필 이미지, 한줄)
     * @return 성공응답
     */
    @PatchMapping("/me")
    @Operation(summary = "유저 정보 수정", description = "현재 로그인한 사용자의 닉네임, 프로필 이미지, 한줄 소개를 수정합니다.")
    public ResponseEntity<?> patchUserInfo(
        @Parameter(description = "수정할 유저 정보 (닉네임, 프로필 이미지, 한줄 소개)")
        @RequestBody UserInfoRequestDto userInfoRequestDto) {
        Long userId = SecurityUtil.getCurrentUserId();

        String nickname = userInfoRequestDto.getNickname();
        String profileImgUrl = userInfoRequestDto.getProfileImgUrl();
        String comment = userInfoRequestDto.getComment();

        UserInfoResponseDto userInfoResponseDto = userService.patchUserInfo(userId, nickname, profileImgUrl, comment);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "유저 정보 수정에 성공하였습니다.", userInfoResponseDto));
    }

}
