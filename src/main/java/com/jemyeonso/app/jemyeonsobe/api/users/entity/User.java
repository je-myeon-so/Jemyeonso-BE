package com.jemyeonso.app.jemyeonsobe.api.users.entity;

import com.jemyeonso.app.jemyeonsobe.api.auth.entity.Oauth;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "users")
public class User {
    @Id // PK
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", length = 30)
    private String nickname;

    @Lob
    @Column(name = "profile_img_url")
    private String profileImgUrl;

    // 개선점
    @Column(name = "improvement", length = 512)
    private String improvement;

    // 한마디
    @Column(name = "comment", length = 100)
    private String comment;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Oauth oauth;
}
