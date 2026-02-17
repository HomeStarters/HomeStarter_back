package com.dwj.homestarter.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 가구원 초대 요청 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdInviteRequest {

    @NotBlank(message = "초대할 사용자 ID는 필수입니다")
    private String targetUserId;
}
