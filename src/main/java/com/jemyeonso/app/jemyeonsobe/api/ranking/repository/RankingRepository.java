package com.jemyeonso.app.jemyeonsobe.api.ranking.repository;

import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<User, Long> {

    // 전체 유저 주간 랭킹 조회
    @Query("SELECT u, " +
            "COALESCE(SUM(i.AvgScore), 0) as totalScore, " +
            "COUNT(CASE WHEN i.AvgScore > 0 THEN i.id END) as interviewCount " +
            "FROM User u " +
            "LEFT JOIN Interview i ON u.id = i.userId " +
            "AND i.createdAt >= :startDate " +
            "AND i.createdAt <= :endDate " +
            "AND i.deletedAt IS NULL " +
            "AND i.AvgScore > 0 " +
            "WHERE u.deletedAt IS NULL " +
            "GROUP BY u.id, u.name, u.nickname, u.profileImgUrl " +
            "HAVING COALESCE(SUM(i.AvgScore), 0) > 0 " +
            "ORDER BY totalScore DESC " +
            "LIMIT :limit")
    List<Object[]> findWeeklyRankings(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("limit") int limit);

    // 특정 유저의 주간 점수 조회
    @Query("SELECT " +
            "COALESCE(SUM(i.AvgScore), 0) as totalScore, " +
            "COUNT(CASE WHEN i.AvgScore > 0 THEN i.id END) as interviewCount " +
            "FROM Interview i " +
            "WHERE i.userId = :userId " +
            "AND i.createdAt >= :startDate " +
            "AND i.createdAt <= :endDate " +
            "AND i.deletedAt IS NULL " +
            "AND i.AvgScore > 0")
    List<Object[]> findWeeklyScoreByUserId(@Param("userId") Long userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}
