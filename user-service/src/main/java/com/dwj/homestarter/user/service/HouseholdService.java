package com.dwj.homestarter.user.service;

import com.dwj.homestarter.user.dto.request.HouseholdInviteRequest;
import com.dwj.homestarter.user.dto.response.HouseholdInvitationResponse;
import com.dwj.homestarter.user.dto.response.HouseholdMemberResponse;

import java.util.List;

/**
 * 가구원 서비스 인터페이스
 *
 * @author homestarter
 * @since 1.0.0
 */
public interface HouseholdService {

    HouseholdInvitationResponse invite(String requesterId, HouseholdInviteRequest request);

    HouseholdInvitationResponse acceptInvitation(String userId, String invitationId);

    HouseholdInvitationResponse rejectInvitation(String userId, String invitationId);

    HouseholdMemberResponse getMembers(String userId);

    void removeMember(String userId, String memberId);

    List<HouseholdInvitationResponse> getPendingInvitations(String userId);

    void delegateOwnership(String currentOwnerId, String newOwnerId);
}
