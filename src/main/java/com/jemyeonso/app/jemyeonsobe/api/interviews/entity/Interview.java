package com.jemyeonso.app.jemyeonsobe.api.interviews.entity;

import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_category", nullable = false)
    private QuestionType questionCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_level", nullable = false)
    private QuestionLevel questionLevel;

    @Column(name = "jobtype", nullable = false, length = 30)
    private String jobtype;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Document와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Enum 정의
    public enum QuestionType {
        압박, 느슨, 기술
    }

    public enum QuestionLevel {
        상, 중, 하
    }
}