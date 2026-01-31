package com.dwj.homestarter.user.repository.jpa;

import com.dwj.homestarter.user.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 기본 정보 저장소
 *
 * 사용자 기본 정보에 대한 데이터베이스 접근을 담당
 *
 * @author homestarter
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 아이디로 사용자 조회
     *
     * @param userId 사용자 아이디
     * @return 사용자 엔티티 Optional
     */
    Optional<UserEntity> findByUserId(String userId);

    /**
     * 이메일로 사용자 조회
     *
     * @param email 이메일
     * @return 사용자 엔티티 Optional
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 아이디 존재 확인
     *
     * @param userId 사용자 아이디
     * @return 존재 여부
     */
    boolean existsByUserId(String userId);

    /**
     * 이메일 존재 확인
     *
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
}
