package com.jemyeonso.app.jemyeonsobe.api.user.service;

import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserFeedbackResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserOverviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.UserDetail;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserDetailRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.AiImproveService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailService {
    private final UserRepository userRepository;
    private final AiImproveService aiImproveService;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailRepository userDetailRepository;

    @Transactional
    public void refreshImprovementFromAi(Long userId, Long interviewId, Long documentId, String jobType) {
        // 해당 사용자 존재 확인
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        // AI에서 개선점
        String overallComment = aiImproveService.fetchOverallComment(interviewId, documentId, jobType);
        log.info("Fetch overall comment from AiImproveClient: {}", overallComment);

        // UserDetail에 저장
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
            .orElseGet(() -> UserDetail.builder()
                .user(user)
                .userId(userId)
                .totalScore(0)
                .build());

        // 개선점 저장
        userDetail.setImprovement(overallComment);
        userDetailRepository.save(userDetail);

        // Redis 반영
        String valKey = "user:" + userId + ":improvement:latest";
        String ptrKey = "user:" + userId + ":improvement:latest:interviewId";

        redisTemplate.opsForValue().set(valKey, overallComment);
        redisTemplate.opsForValue().set(ptrKey, String.valueOf(interviewId));
    }

    public UserFeedbackResponseDto getImprovement(Long userId) {
        // 유저 찾기
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        String key = "user:" + user.getId() + ":improvement:latest";
        String cachedValue = redisTemplate.opsForValue().get(key);

        // Redis에 저장되어 있으면 캐시된 값 사용
        if (cachedValue != null) {
            return UserFeedbackResponseDto.builder()
                .userId(user.getId())
                .feedback(cachedValue)
                .build();
        }

        // 캐시에 없으면 DB (UserDetail) 값 사용
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_DETAIL_NOT_FOUND));

        String dbValue = userDetail.getImprovement();

        if (dbValue != null && !dbValue.isBlank()) {
            redisTemplate.opsForValue().set(key, dbValue);
        }

        return UserFeedbackResponseDto.builder()
            .userId(user.getId())
            .feedback(userDetail.getImprovement())
            .build();
    }

    /**
     * 유저 한줄소개 조회
     * @param userId
     * @return
     */
    public UserOverviewResponseDto getOverview(Long userId) {
        // 유저 디테일 조회
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_DETAIL_NOT_FOUND));

        // 유저 한줄소개 반환
        return UserOverviewResponseDto.builder()
            .overview(userDetail.getOverview())
            .build();
    }

    @Transactional
    public UserOverviewResponseDto patchOverview(Long userId, String overview) {
        // 유저 디테일 조회
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
            .orElseThrow(()->new ResourceNotFoundException(ErrorMessage.USER_DETAIL_NOT_FOUND));

        // 한줄소개 수정
        userDetail.setOverview(overview);
        userDetailRepository.save(userDetail);

        // 유저 한줄소개 반환
        return UserOverviewResponseDto.builder()
            .overview(userDetail.getOverview())
            .build();
    }
}
