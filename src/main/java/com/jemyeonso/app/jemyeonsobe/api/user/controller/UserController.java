package com.jemyeonso.app.jemyeonsobe.api.user.controller;

import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserInfoRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserInfoResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.service.UserService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.common.exception.ApiResponse;
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
public class UserController {

    private final UserService userService;

    /**
     * 유저 정보 조회 API
     * @return 성공응답
     */
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "유저 정보 조회에 성공하였습니다.", userInfoResponseDto));
    }

    /**
     * 유저 정보 수정 API
     * @param userInfoRequestDto (닉네임, 유저 프로필 이미지, 한줄)
     * @return 성공응답
     */
    @PatchMapping("/me")
    public ResponseEntity<?> patchUserInfo(@RequestBody UserInfoRequestDto userInfoRequestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String nickname = userInfoRequestDto.getNickname();
        String profileImgUrl = userInfoRequestDto.getProfileImgUrl();
        String comment = userInfoRequestDto.getComment();

        UserInfoResponseDto userInfoResponseDto = userService.patchUserInfo(userId, nickname, profileImgUrl, comment);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "유저 정보 수정에 성공하였습니다.", userInfoResponseDto));
    }

}
