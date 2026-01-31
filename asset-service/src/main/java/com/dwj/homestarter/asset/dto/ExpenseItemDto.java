package com.dwj.homestarter.asset.dto;

import com.dwj.homestarter.asset.domain.ExpenseItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 월지출 항목 DTO
 * 월지출 항목 정보 전달용 데이터 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseItemDto {

    /**
     * 월지출 항목 ID (응답 시에만 사용)
     */
    private String id;

    /**
     * 지출명 (예: 생활비)
     */
    @NotBlank(message = "지출명은 필수입니다")
    private String name;

    /**
     * 월 금액 (원)
     */
    @NotNull(message = "월 지출 금액은 필수입니다")
    @PositiveOrZero(message = "월 지출 금액은 0 이상이어야 합니다")
    private Long amount;

    /**
     * 도메인 객체로 변환
     *
     * @return ExpenseItem 도메인 객체
     */
    public ExpenseItem toDomain() {
        return ExpenseItem.builder()
                .id(id != null ? id : UUID.randomUUID().toString())
                .name(name)
                .amount(amount)
                .build();
    }

    /**
     * 도메인 객체로부터 DTO 생성
     *
     * @param item ExpenseItem 도메인 객체
     * @return ExpenseItemDto
     */
    public static ExpenseItemDto fromDomain(ExpenseItem item) {
        return ExpenseItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .amount(item.getAmount())
                .build();
    }
}
