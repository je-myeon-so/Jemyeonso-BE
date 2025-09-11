package com.jemyeonso.app.jemyeonsobe.api.ranking.scheduler;

import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.UserDetail;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserDetailRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyScoreUpdateScheduler {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final UserDetailRepository userDetailRepository;

    // 1분마다 모든 사용자의 주간 점수 업데이트 (폴링)
    @Scheduled(fixedRate = 60000) // 300초 = 1분
    @Transactional
    public void updateAllUsersWeeklyScores() {
        log.info("주간 점수 업데이트 시작");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusWeeks(1);

        try {
            List<Object[]> weeklyScores = interviewRepository.calculateAllUsersWeeklyScores(weekAgo, now);
            Map<Long, Integer> userScoreMap = weeklyScores.stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> ((Number) row[1]).intValue()
                    ));

            List<UserDetail> allUserDetails = userDetailRepository.findAll();
            Map<Long, UserDetail> userDetailMap = allUserDetails.stream()
                    .collect(Collectors.toMap(UserDetail::getUserId, Function.identity()));

            List<User> allUsers = userRepository.findByDeletedAtIsNull();

            List<UserDetail> toUpdate = new ArrayList<>();
            List<UserDetail> toCreate = new ArrayList<>();

            for (User user : allUsers) {
                Integer weeklyScore = userScoreMap.getOrDefault(user.getId(), 0);
                UserDetail userDetail = userDetailMap.get(user.getId());

                if (userDetail == null) {
                    userDetail = UserDetail.builder()
                            .user(user)
                            .userId(user.getId())
                            .totalScore(weeklyScore)
                            .build();
                    toCreate.add(userDetail);
                } else if (!weeklyScore.equals(userDetail.getTotalScore())) {
                    userDetail.setTotalScore(weeklyScore);
                    toUpdate.add(userDetail);
                }
            }

            if (!toCreate.isEmpty()) {
                userDetailRepository.saveAll(toCreate);
            }
            if (!toUpdate.isEmpty()) {
                userDetailRepository.saveAll(toUpdate);
            }

            log.info("주간 점수 업데이트 완료: {} 명 생성, {} 명 업데이트",
                    toCreate.size(), toUpdate.size());

        } catch (Exception e) {
            log.error("주간 점수 업데이트 중 오류 발생", e);
        }
    }

    // 매주 월요일 오전 12시에 모든 totalScore 초기화
    @Scheduled(cron = "0 0 0 * * MON")
    @Transactional
    public void resetWeeklyScores() {
        log.info("주간 점수 초기화 시작");

        try {
            int updatedCount = userDetailRepository.resetAllTotalScores();
            log.info("주간 점수 초기화 완료: {} 명의 사용자", updatedCount);
        } catch (Exception e) {
            log.error("주간 점수 초기화 중 오류 발생", e);
        }
    }
}
