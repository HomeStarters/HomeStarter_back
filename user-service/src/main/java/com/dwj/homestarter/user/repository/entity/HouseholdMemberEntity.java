package com.dwj.homestarter.user.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 가구원 매핑 엔티티
 *
 * 동일 가구(household)에 속한 사용자들을 관리
 *
 * @author homestarter
 * @since 1.0.0
 */
@Entity
@Table(name = "household_members", schema = "user_service",
        uniqueConstraints = @UniqueConstraint(columnNames = {"household_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HouseholdMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "household_id", nullable = false, length = 50)
    private String householdId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private String role = "MEMBER";

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void changeRole(String role) {
        this.role = role;
    }
}
