package com.jemyeonso.app.jemyeonsobe.api.ranking.repository;

import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<User, Long> {

    // UserDetail 테이블의 totalScore 기준으로 상위 유저 조회
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userDetail ud " +
            "WHERE u.deletedAt IS NULL " +
            "AND ud.totalScore IS NOT NULL " +
            "AND ud.totalScore > 0 " +
            "ORDER BY ud.totalScore DESC")
    List<User> findTopUsersByTotalScore();

    // 특정 유저의 랭킹 조회
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userDetail ud " +
            "WHERE u.deletedAt IS NULL " +
            "AND ud.totalScore IS NOT NULL " +
            "AND ud.totalScore > 0 " +
            "ORDER BY ud.totalScore DESC")
    Page<User> findTopUsersByTotalScore(Pageable pageable);

    @Query(value = "SELECT COUNT(*) + 1 " +
            "FROM user_detail ud1 " +
            "INNER JOIN user_detail ud2 ON ud2.user_id = :userId " +
            "INNER JOIN users u ON u.id = ud1.user_id " +
            "WHERE u.deleted_at IS NULL " +
            "AND ud1.total_score > ud2.total_score",
            nativeQuery = true)
    Integer findUserRankByTotalScore(@Param("userId") Long userId);
}
