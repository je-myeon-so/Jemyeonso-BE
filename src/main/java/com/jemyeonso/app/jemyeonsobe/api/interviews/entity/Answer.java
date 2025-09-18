package com.jemyeonso.app.jemyeonsobe.api.interviews.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "answer_time")
    private String answerTime;

    @Column(name = "score", nullable = false)
    @ColumnDefault("0")
    private Integer score;

    // Feedback과의 연관관계
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "answer_id")
    private List<Feedback> feedbacks = new ArrayList<>();
}
