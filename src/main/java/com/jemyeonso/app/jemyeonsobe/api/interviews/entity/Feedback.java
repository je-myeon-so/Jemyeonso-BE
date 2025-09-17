package com.jemyeonso.app.jemyeonsobe.api.interviews.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_id", nullable = false)
    private Long answerId;

    @Column(name = "feedback", nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "error_text", nullable = false, columnDefinition = "TEXT")
    private String errorText;

    @Column(name = "suggestion", nullable = false, columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "error_type", nullable = false, columnDefinition = "TEXT")
    private String errorType;
}
