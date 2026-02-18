package com.dwj.homestarter.user.repository.jpa;

import com.dwj.homestarter.user.repository.entity.HouseholdInvitationEntity;
import com.dwj.homestarter.user.repository.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 가구원 초대 리포지토리
 *
 * @author homestarter
 * @since 1.0.0
 */
@Repository
public interface HouseholdInvitationRepository extends JpaRepository<HouseholdInvitationEntity, Long> {

    Optional<HouseholdInvitationEntity> findByInvitationId(String invitationId);

    List<HouseholdInvitationEntity> findByTargetUserIdAndStatus(String targetUserId, InvitationStatus status);

    List<HouseholdInvitationEntity> findByRequesterUserIdOrTargetUserId(String requesterUserId, String targetUserId);

    boolean existsByRequesterUserIdAndTargetUserIdAndStatus(String requesterUserId, String targetUserId, InvitationStatus status);
}
