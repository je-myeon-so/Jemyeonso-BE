package com.jemyeonso.app.jemyeonsobe.api.ranking.repository;

import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<User, Long> {

    // User 테이블의 totalScore 기준으로 상위 유저 조회
    @Query("SELECT u FROM User u " +
            "WHERE u.deletedAt IS NULL " +
            "AND u.totalScore > 0 " +
            "ORDER BY u.totalScore DESC")
    List<User> findTopUsersByTotalScore(Pageable pageable);

    // 특정 유저의 랭킹 조회
    @Query("SELECT COUNT(u) + 1 FROM User u " +
            "WHERE u.deletedAt IS NULL " +
            "AND u.totalScore > (SELECT u2.totalScore FROM User u2 WHERE u2.id = :userId)")
    Integer findUserRankByTotalScore(@Param("userId") Long userId);
}
