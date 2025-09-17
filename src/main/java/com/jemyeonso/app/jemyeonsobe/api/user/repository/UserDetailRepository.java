package com.jemyeonso.app.jemyeonsobe.api.user.repository;

import com.jemyeonso.app.jemyeonsobe.api.user.entity.UserDetail;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserDetail ud SET ud.totalScore = 0 WHERE ud.totalScore != 0")
    int resetAllTotalScores();


    // 동시성 제어하기 위해 비관적 락 적용 (동시 업데이트 직렬화)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserDetail u where u.userId = :userId")
    Optional<UserDetail> findByUserIdForUpdate(Long userId);

}
