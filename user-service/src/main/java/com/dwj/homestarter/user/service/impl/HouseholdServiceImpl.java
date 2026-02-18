package com.dwj.homestarter.user.service.impl;

import com.dwj.homestarter.common.exception.BusinessException;
import com.dwj.homestarter.common.exception.NotFoundException;
import com.dwj.homestarter.user.dto.request.HouseholdInviteRequest;
import com.dwj.homestarter.user.dto.response.HouseholdInvitationResponse;
import com.dwj.homestarter.user.dto.response.HouseholdMemberResponse;
import com.dwj.homestarter.user.event.HouseholdEventPublisher;
import com.dwj.homestarter.user.repository.entity.*;
import com.dwj.homestarter.user.repository.jpa.HouseholdInvitationRepository;
import com.dwj.homestarter.user.repository.jpa.HouseholdMemberRepository;
import com.dwj.homestarter.user.repository.jpa.UserRepository;
import com.dwj.homestarter.user.service.HouseholdService;
import com.dwj.homestarter.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 가구원 서비스 구현체
 *
 * 가구원 초대, 수락, 거절, 목록 조회, 제거 등의 비즈니스 로직 처리
 *
 * @author homestarter
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseholdServiceImpl implements HouseholdService {

    private final UserRepository userRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdInvitationRepository householdInvitationRepository;
    private final NotificationService notificationService;
    private final HouseholdEventPublisher householdEventPublisher;

    @Override
    @Transactional
    public HouseholdInvitationResponse invite(String requesterId, HouseholdInviteRequest request) {
        String targetUserId = request.getTargetUserId();

        // 자기 자신에게 초대 불가
        if (requesterId.equals(targetUserId)) {
            throw new BusinessException("HOUSEHOLD_001", "자기 자신을 가구원으로 초대할 수 없습니다");
        }

        // 대상 사용자 존재 확인
        UserEntity targetUser = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new NotFoundException("초대할 사용자를 찾을 수 없습니다 (userId: " + targetUserId + ")"));

        UserEntity requester = userRepository.findByUserId(requesterId)
                .orElseThrow(() -> new NotFoundException("요청자 정보를 찾을 수 없습니다"));

        // 이미 같은 가구원인지 확인
        List<HouseholdMemberEntity> requesterHouseholds = householdMemberRepository.findByUserId(requesterId);
        for (HouseholdMemberEntity memberEntity : requesterHouseholds) {
            if (householdMemberRepository.existsByHouseholdIdAndUserId(memberEntity.getHouseholdId(), targetUserId)) {
                throw new BusinessException("HOUSEHOLD_002", "이미 같은 가구에 속한 사용자입니다");
            }
        }

        // 이미 속한 가구가 있는지 확인 (개발필요)

        // 이미 PENDING 상태인 초대가 있는지 확인
        if (householdInvitationRepository.existsByRequesterUserIdAndTargetUserIdAndStatus(
                requesterId, targetUserId, InvitationStatus.PENDING)) {
            throw new BusinessException("HOUSEHOLD_003", "이미 대기 중인 초대가 있습니다");
        }

        // 초대 생성
        String invitationId = UUID.randomUUID().toString();
        HouseholdInvitationEntity invitation = HouseholdInvitationEntity.builder()
                .invitationId(invitationId)
                .requesterUserId(requesterId)
                .targetUserId(targetUserId)
                .build();

        householdInvitationRepository.save(invitation);

        // 대상자에게 알림 생성
        notificationService.createNotification(
                targetUserId,
                NotificationType.HOUSEHOLD_INVITATION,
                "가구원 등록 요청",
                requester.getName() + "님이 가구원 등록을 요청했습니다",
                invitationId
        );

        log.info("가구원 초대 생성 - requester: {}, target: {}, invitationId: {}", requesterId, targetUserId, invitationId);

        return toInvitationResponse(invitation, requester.getName(), targetUser.getName());
    }

    @Override
    @Transactional
    public HouseholdInvitationResponse acceptInvitation(String userId, String invitationId) {
        HouseholdInvitationEntity invitation = householdInvitationRepository.findByInvitationId(invitationId)
                .orElseThrow(() -> new NotFoundException("초대를 찾을 수 없습니다"));

        // 본인에게 온 초대인지 확인
        if (!invitation.getTargetUserId().equals(userId)) {
            throw new BusinessException("HOUSEHOLD_004", "본인에게 온 초대만 수락할 수 있습니다");
        }

        // PENDING 상태인지 확인
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("HOUSEHOLD_005", "이미 처리된 초대입니다");
        }

        // 초대 수락
        invitation.accept();

        // 가구원 등록 처리
        String householdId = resolveHouseholdId(invitation.getRequesterUserId());

        // 요청자가 이미 가구에 속해있지 않으면 요청자도 등록
        if (!householdMemberRepository.existsByHouseholdIdAndUserId(householdId, invitation.getRequesterUserId())) {
            HouseholdMemberEntity requesterMember = HouseholdMemberEntity.builder()
                    .householdId(householdId)
                    .userId(invitation.getRequesterUserId())
                    .role("OWNER")
                    .joinedAt(LocalDateTime.now())
                    .build();
            householdMemberRepository.save(requesterMember);
        }

        // 대상자 등록
        HouseholdMemberEntity targetMember = HouseholdMemberEntity.builder()
                .householdId(householdId)
                .userId(userId)
                .role("MEMBER")
                .joinedAt(LocalDateTime.now())
                .build();
        householdMemberRepository.save(targetMember);

        // 요청자에게 수락 알림
        UserEntity targetUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        notificationService.createNotification(
                invitation.getRequesterUserId(),
                NotificationType.HOUSEHOLD_ACCEPTED,
                "가구원 등록 완료",
                targetUser.getName() + "님이 가구원 등록 요청을 수락했습니다",
                invitationId
        );

        // Kafka 이벤트 발행
        List<String> memberUserIds = householdMemberRepository.findByHouseholdId(householdId).stream()
                .map(HouseholdMemberEntity::getUserId)
                .toList();

        householdEventPublisher.publishMemberJoined(
                householdId, invitation.getRequesterUserId(), userId, memberUserIds);

        log.info("가구원 초대 수락 - invitationId: {}, householdId: {}, acceptedBy: {}", invitationId, householdId, userId);

        UserEntity requester = userRepository.findByUserId(invitation.getRequesterUserId())
                .orElseThrow(() -> new NotFoundException("요청자 정보를 찾을 수 없습니다"));

        return toInvitationResponse(invitation, requester.getName(), targetUser.getName());
    }

    @Override
    @Transactional
    public HouseholdInvitationResponse rejectInvitation(String userId, String invitationId) {
        HouseholdInvitationEntity invitation = householdInvitationRepository.findByInvitationId(invitationId)
                .orElseThrow(() -> new NotFoundException("초대를 찾을 수 없습니다"));

        if (!invitation.getTargetUserId().equals(userId)) {
            throw new BusinessException("HOUSEHOLD_004", "본인에게 온 초대만 거절할 수 있습니다");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException("HOUSEHOLD_005", "이미 처리된 초대입니다");
        }

        invitation.reject();

        // 요청자에게 거절 알림
        UserEntity targetUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다"));

        notificationService.createNotification(
                invitation.getRequesterUserId(),
                NotificationType.HOUSEHOLD_REJECTED,
                "가구원 등록 거절",
                targetUser.getName() + "님이 가구원 등록 요청을 거절했습니다",
                invitationId
        );

        log.info("가구원 초대 거절 - invitationId: {}, rejectedBy: {}", invitationId, userId);

        UserEntity requester = userRepository.findByUserId(invitation.getRequesterUserId())
                .orElseThrow(() -> new NotFoundException("요청자 정보를 찾을 수 없습니다"));

        return toInvitationResponse(invitation, requester.getName(), targetUser.getName());
    }

    @Override
    public HouseholdMemberResponse getMembers(String userId) {
        List<HouseholdMemberEntity> userHouseholds = householdMemberRepository.findByUserId(userId);

        if (userHouseholds.isEmpty()) {
            return HouseholdMemberResponse.builder()
                    .members(List.of())
                    .build();
        }

        // 첫 번째 가구 기준 (현재 1인 1가구 가정)
        String householdId = userHouseholds.getFirst().getHouseholdId();
        List<HouseholdMemberEntity> members = householdMemberRepository.findByHouseholdId(householdId);

        List<HouseholdMemberResponse.MemberInfo> memberInfos = members.stream()
                .map(member -> {
                    UserEntity user = userRepository.findByUserId(member.getUserId()).orElse(null);
                    return HouseholdMemberResponse.MemberInfo.builder()
                            .userId(member.getUserId())
                            .name(user != null ? user.getName() : member.getUserId())
                            .email(user != null ? user.getEmail() : null)
                            .role(member.getRole())
                            .joinedAt(member.getJoinedAt())
                            .build();
                })
                .toList();

        return HouseholdMemberResponse.builder()
                .householdId(householdId)
                .members(memberInfos)
                .build();
    }

    @Override
    @Transactional
    public void removeMember(String userId, String memberId) {
        List<HouseholdMemberEntity> memberHouseholds = householdMemberRepository.findByUserId(memberId);
        if (memberHouseholds.isEmpty()) {
            throw new NotFoundException("가구원을 찾을 수 없습니다");
        }
        HouseholdMemberEntity member = memberHouseholds.getFirst();

        // 본인이 해당 가구에 속해있는지 확인
        String householdId = member.getHouseholdId();
        if (!householdMemberRepository.existsByHouseholdIdAndUserId(householdId, userId)) {
            throw new BusinessException("HOUSEHOLD_006", "해당 가구의 가구원만 제거할 수 있습니다");
        }

        String removedUserId = member.getUserId();
        householdMemberRepository.delete(member);

        // 남은 가구원 목록으로 이벤트 발행
        List<String> remainingMemberIds = householdMemberRepository.findByHouseholdId(householdId).stream()
                .map(HouseholdMemberEntity::getUserId)
                .toList();

        householdEventPublisher.publishMemberLeft(householdId, removedUserId, remainingMemberIds);

        log.info("가구원 제거 - householdId: {}, removedUserId: {}, removedBy: {}", householdId, removedUserId, userId);
    }

    @Override
    public List<HouseholdInvitationResponse> getPendingInvitations(String userId) {
        return householdInvitationRepository.findByTargetUserIdAndStatus(userId, InvitationStatus.PENDING).stream()
                .map(invitation -> {
                    String requesterName = userRepository.findByUserId(invitation.getRequesterUserId())
                            .map(UserEntity::getName).orElse(invitation.getRequesterUserId());
                    String targetName = userRepository.findByUserId(invitation.getTargetUserId())
                            .map(UserEntity::getName).orElse(invitation.getTargetUserId());
                    return toInvitationResponse(invitation, requesterName, targetName);
                })
                .toList();
    }

    @Override
    @Transactional
    public void delegateOwnership(String currentOwnerId, String newOwnerId) {
        // 현재 OWNER의 가구 조회
        List<HouseholdMemberEntity> ownerHouseholds = householdMemberRepository.findByUserId(currentOwnerId);
        if (ownerHouseholds.isEmpty()) {
            throw new NotFoundException("가구에 속해있지 않습니다");
        }

        HouseholdMemberEntity ownerMember = ownerHouseholds.getFirst();
        String householdId = ownerMember.getHouseholdId();

        // OWNER 권한 확인
        if (!"OWNER".equals(ownerMember.getRole())) {
            throw new BusinessException("HOUSEHOLD_007", "OWNER 권한을 가진 사용자만 권한을 위임할 수 있습니다");
        }

        // 위임 대상이 같은 가구의 MEMBER인지 확인
        HouseholdMemberEntity targetMember = householdMemberRepository
                .findByHouseholdIdAndUserId(householdId, newOwnerId)
                .orElseThrow(() -> new BusinessException("HOUSEHOLD_008", "같은 가구에 속한 가구원에게만 권한을 위임할 수 있습니다"));

        // 권한 교환: 기존 OWNER → MEMBER, 대상 MEMBER → OWNER
        ownerMember.changeRole("MEMBER");
        targetMember.changeRole("OWNER");

        log.info("OWNER 권한 위임 - householdId: {}, {} → {}", householdId, currentOwnerId, newOwnerId);
    }

    /**
     * 요청자의 기존 가구 ID를 찾거나 새로 생성
     */
    private String resolveHouseholdId(String requesterUserId) {
        List<HouseholdMemberEntity> existing = householdMemberRepository.findByUserId(requesterUserId);
        if (!existing.isEmpty()) {
            return existing.get(0).getHouseholdId();
        }
        return UUID.randomUUID().toString();
    }

    private HouseholdInvitationResponse toInvitationResponse(HouseholdInvitationEntity entity, String requesterName, String targetName) {
        return HouseholdInvitationResponse.builder()
                .invitationId(entity.getInvitationId())
                .requesterUserId(entity.getRequesterUserId())
                .requesterName(requesterName)
                .targetUserId(entity.getTargetUserId())
                .targetName(targetName)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .respondedAt(entity.getRespondedAt())
                .build();
    }
}
