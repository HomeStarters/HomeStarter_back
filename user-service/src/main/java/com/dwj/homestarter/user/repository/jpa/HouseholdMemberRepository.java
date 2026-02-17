package com.dwj.homestarter.user.repository.jpa;

import com.dwj.homestarter.user.repository.entity.HouseholdMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 가구원 매핑 리포지토리
 *
 * @author homestarter
 * @since 1.0.0
 */
@Repository
public interface HouseholdMemberRepository extends JpaRepository<HouseholdMemberEntity, Long> {

    List<HouseholdMemberEntity> findByUserId(String userId);

    List<HouseholdMemberEntity> findByHouseholdId(String householdId);

    Optional<HouseholdMemberEntity> findByHouseholdIdAndUserId(String householdId, String userId);

    boolean existsByHouseholdIdAndUserId(String householdId, String userId);

    void deleteByHouseholdIdAndUserId(String householdId, String userId);
}
