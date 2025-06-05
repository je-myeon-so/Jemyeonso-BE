package com.jemyeonso.app.jemyeonsobe.api.interviews.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "error_text", nullable = false, columnDefinition = "TEXT")
    private String errorText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "error_type", nullable = false, columnDefinition = "TEXT")
    private String errorType;
}
