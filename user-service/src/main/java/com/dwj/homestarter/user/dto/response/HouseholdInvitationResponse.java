package com.dwj.homestarter.user.dto.response;

import com.dwj.homestarter.user.repository.entity.InvitationStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 가구원 초대 응답 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdInvitationResponse {

    private String invitationId;
    private String requesterUserId;
    private String requesterName;
    private String targetUserId;
    private String targetName;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
