package com.dwj.homestarter.roadmap.dto.request;

import com.dwj.homestarter.roadmap.repository.entity.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 생애주기 이벤트 등록/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifecycleEventRequest {

    /**
     * 이벤트 이름
     */
    @NotBlank(message = "이벤트 이름은 필수입니다")
    @Size(max = 100, message = "이벤트 이름은 100자 이하여야 합니다")
    private String name;

    /**
     * 이벤트 유형
     */
    @NotNull(message = "이벤트 유형은 필수입니다")
    private EventType eventType;

    /**
     * 이벤트 예정일 (yyyy-MM 형식)
     */
    @NotBlank(message = "이벤트 예정일은 필수입니다")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "이벤트 예정일은 yyyy-MM 형식이어야 합니다")
    private String eventDate;

    /**
     * 주택 선택 고려 기준
     */
    @Size(max = 200, message = "주택 선택 고려 기준은 200자 이하여야 합니다")
    private String housingCriteria;
}
