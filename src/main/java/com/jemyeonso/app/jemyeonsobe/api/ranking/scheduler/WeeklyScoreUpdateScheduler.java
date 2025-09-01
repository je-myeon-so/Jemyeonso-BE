package com.jemyeonso.app.jemyeonsobe.api.ranking.scheduler;

import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyScoreUpdateScheduler {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;

    // 1분마다 모든 사용자의 주간 점수 업데이트 (폴링)
    @Scheduled(fixedRate = 60000) // 300초 = 1분
    @Transactional
    public void updateAllUsersWeeklyScores() {
        log.info("주간 점수 업데이트 시작");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusWeeks(1);

        try {
            List<User> allUsers = userRepository.findByDeletedAtIsNull();
            int updatedCount = 0;

            for (User user : allUsers) {
                // 각 사용자의 주간 점수 계산
                Integer weeklyScore = interviewRepository.calculateWeeklyScoreByUserId(
                        user.getId(), weekAgo, now
                );

                // totalScore 업데이트
                if (!weeklyScore.equals(user.getTotalScore())) {
                    user.setTotalScore(weeklyScore);
                    userRepository.save(user);
                    updatedCount++;
                }
            }

            log.info("주간 점수 업데이트 완료: {} 명의 사용자 업데이트됨", updatedCount);

        } catch (Exception e) {
            log.error("주간 점수 업데이트 중 오류 발생", e);
        }
    }

    // 매주 월요일 오전 12시에 모든 totalScore 초기화
    @Scheduled(cron = "0 0 12 * * MON")
    @Transactional
    public void resetWeeklyScores() {
        log.info("주간 점수 초기화 시작");

        try {
            int updatedCount = userRepository.resetAllTotalScores();
            log.info("주간 점수 초기화 완료: {} 명의 사용자", updatedCount);
        } catch (Exception e) {
            log.error("주간 점수 초기화 중 오류 발생", e);
        }
    }
}
