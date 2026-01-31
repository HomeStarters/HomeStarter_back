package com.dwj.homestarter.user.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 기본 정보 엔티티
 *
 * 사용자 계정 기본 정보를 저장하는 엔티티
 *
 * @author homestarter
 * @since 1.0.0
 */
@Entity
@Table(name = "users", schema = "user_service")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * 기본키 (자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 아이디 (로그인용)
     */
    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    /**
     * 사용자 이름
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 이메일 주소
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 전화번호
     */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /**
     * BCrypt 암호화된 비밀번호
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 사용자 역할 (USER, ADMIN)
     */
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";

    /**
     * 마지막 로그인 시간
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
