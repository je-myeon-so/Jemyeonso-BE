package com.jemyeonso.app.jemyeonsobe.api.interviews.repository;

import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    @Query("SELECT i FROM Interview i JOIN FETCH i.document ORDER BY i.createdAt DESC")
    Page<Interview> findAllWithDocument(Pageable pageable);

    // 특정 유저의 면접만 조회 (Document와 함께)
    @Query("SELECT i FROM Interview i JOIN FETCH i.document WHERE i.userId = :userId ORDER BY i.createdAt DESC")
    Page<Interview> findByUserIdWithDocument(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.AvgScore), 0) FROM Interview i " +
            "WHERE i.userId = :userId " +
            "AND i.createdAt >= :startDate " +
            "AND i.createdAt <= :endDate " +
            "AND i.deletedAt IS NULL " +
            "AND i.AvgScore > 0")
    Integer calculateWeeklyScoreByUserId(@Param("userId") Long userId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i.userId, COALESCE(SUM(i.AvgScore), 0) " +
            "FROM Interview i " +
            "WHERE i.createdAt >= :startDate " +
            "AND i.createdAt <= :endDate " +
            "AND i.deletedAt IS NULL " +
            "AND i.AvgScore > 0 " +
            "GROUP BY i.userId")
    List<Object[]> calculateAllUsersWeeklyScores(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);


    // 최신 면접 조회 메서드 추가
    @Query("SELECT i FROM Interview i WHERE i.userId = :userId ORDER BY i.createdAt DESC")
    List<Interview> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    default Optional<Interview> findLatestByUserId(Long userId) {
        List<Interview> list = findTopByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

}
