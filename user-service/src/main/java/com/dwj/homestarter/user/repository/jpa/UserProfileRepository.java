package com.dwj.homestarter.user.repository.jpa;

import com.dwj.homestarter.user.repository.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 프로필 정보 저장소
 *
 * 사용자 프로필 정보에 대한 데이터베이스 접근을 담당
 *
 * @author homestarter
 * @since 1.0.0
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

    /**
     * 사용자 ID로 프로필 조회
     *
     * @param userId 사용자 아이디
     * @return 프로필 엔티티 Optional
     */
    Optional<UserProfileEntity> findByUserId(String userId);

    /**
     * 사용자 ID로 프로필 삭제
     *
     * @param userId 사용자 아이디
     */
    void deleteByUserId(String userId);
}
