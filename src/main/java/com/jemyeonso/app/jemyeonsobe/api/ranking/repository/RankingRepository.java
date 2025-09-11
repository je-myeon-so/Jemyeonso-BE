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

    // UserDetail 테이블의 totalScore 기준으로 상위 유저 조회
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userDetail ud " +
            "WHERE u.deletedAt IS NULL " +
            "AND ud.totalScore IS NOT NULL " +
            "AND ud.totalScore > 0 " +
            "ORDER BY ud.totalScore DESC")
    List<User> findTopUsersByTotalScore();

    // 특정 유저의 랭킹 조회
    @Query("SELECT COUNT(u) + 1 FROM User u " +
            "LEFT JOIN u.userDetail ud " +
            "LEFT JOIN UserDetail ud2 ON ud2.userId = :userId " +
            "WHERE u.deletedAt IS NULL " +
            "AND ud.totalScore > ud2.totalScore")
    Integer findUserRankByTotalScore(@Param("userId") Long userId);
}
