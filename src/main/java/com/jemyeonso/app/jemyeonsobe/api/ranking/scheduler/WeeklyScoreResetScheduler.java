package com.jemyeonso.app.jemyeonsobe.api.ranking.scheduler;

import com.jemyeonso.app.jemyeonsobe.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyScoreResetScheduler {

    private final UserRepository userRepository;

    // 매주 월요일 오전 12시에 실행
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
