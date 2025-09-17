package com.jemyeonso.app.jemyeonsobe.api.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@DynamicInsert
@Table(name = "user_detail")
public class UserDetail {
    @Id
    @Column(name = "id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // 한줄소개
    @Column(name = "overview", length = 100)
    private String overview;

    // 면접 점수 총점
    @Column(name = "total_score", nullable = false)
    @ColumnDefault("0")
    private Integer totalScore;

    // 개선점
    @Column(name = "improvement", length = 512)
    private String improvement;
}
