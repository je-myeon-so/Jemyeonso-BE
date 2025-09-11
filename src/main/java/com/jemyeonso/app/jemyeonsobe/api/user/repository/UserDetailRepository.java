package com.jemyeonso.app.jemyeonsobe.api.user.repository;

import com.jemyeonso.app.jemyeonsobe.api.user.entity.UserDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserDetail ud SET ud.totalScore = 0 WHERE ud.totalScore != 0")
    int resetAllTotalScores();
}
