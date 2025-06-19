package com.jemyeonso.app.jemyeonsobe.api.user.service;

import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserFeedbackResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserInfoResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        return UserInfoResponseDto.builder()
            .userId(user.getId())
            .profileImgUrl(user.getProfileImgUrl())
            .name(user.getName())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .comment(user.getComment())
            .build();
    }

    public UserInfoResponseDto patchUserInfo(Long userId, String nickname, String profileImgUrl, String comment) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        if (nickname != null) user.setNickname(nickname);
        if (profileImgUrl != null) user.setProfileImgUrl(profileImgUrl);
        if (comment != null) user.setComment(comment);

        return UserInfoResponseDto.builder()
            .userId(user.getId())
            .profileImgUrl(user.getProfileImgUrl())
            .name(user.getName())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .comment(user.getComment())
            .build();
    }

    public UserFeedbackResponseDto getImprovement(Long userId) {
        // 유저 찾기
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        return UserFeedbackResponseDto.builder()
            .userId(user.getId())
            .feedback(user.getImprovement())
            .build();
    }
}
