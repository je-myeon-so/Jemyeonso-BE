package com.jemyeonso.app.jemyeonsobe.api.user.service;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.QaListProvider;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserFeedbackResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.dto.UserOverviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.UserDetail;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserDetailRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.AiImproveService;
import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto.ImproveRequestDto.QaItem;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailService {
    private final UserRepository userRepository;
    private final AiImproveService aiImproveService;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailRepository userDetailRepository;
    private final QaListProvider qaListProvider;
    private final InterviewRepository interviewRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshImprovementFromAi(Long userId, Long interviewId) {
        // 로깅
        log.info("refreshImprovementFromAi");

        // 해당 사용자 존재 확인
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        // Interview, Jobtype 가져오기
        Interview interview = interviewRepository.findByIdAndDeletedAtIsNull(interviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.INTERVIEW_NOT_FOUND));

        // 권한 검증
        if (!interview.getUserId().equals(userId)) {
            throw new SecurityException(ErrorMessage.NO_INTERVIEW_PERMISSION.getMessage());
        }

        // DB에서 인터뷰별 QA 리스트 뽑아오기
        var qaList = qaListProvider.getQaListForInterview(interviewId);

        if (qaList.isEmpty() || qaList == null) {
            throw new ResourceNotFoundException(ErrorMessage.INTERVIEW_QA_NOT_FOUND);
        }

        // 확인용
        for (QaItem qaItem : qaList) {
            System.out.println("qaItem: " + qaItem);
        }

        String jobType = interview.getJobtype();

        // interviewID, JobType, Qa 리스트 전송
        String overallComment = aiImproveService.fetchOverallComment(interviewId, jobType, qaList);
        log.info("AI로부터 개선점 가져오기 성공: {}", overallComment);

        // 비관적 락을 걸고 가져온다.
        UserDetail userDetail = userDetailRepository.findByUserIdForUpdate(userId)
            .orElse(null);

        log.info("--- userDetail: {} ---", userDetail);
        if (userDetail == null) {
            // 엔티티가 없으면 새로
            userDetail = UserDetail.builder()
                .user(user)
                .totalScore(0)
                .improvement(overallComment)
                .build();
            userDetailRepository.saveAndFlush(userDetail);
        } else {
            // 기존 있으면
            userDetail.setImprovement(overallComment);
            userDetailRepository.saveAndFlush(userDetail);
        }


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
