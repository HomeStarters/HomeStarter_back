package com.dwj.homestarter.asset.dto;

import com.dwj.homestarter.asset.domain.IncomeItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 월소득 항목 DTO
 * 월소득 항목 정보 전달용 데이터 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeItemDto {

    /**
     * 월소득 항목 ID (응답 시에만 사용)
     */
    private String id;

    /**
     * 소득명 (예: 회사 급여)
     */
    @NotBlank(message = "소득명은 필수입니다")
    private String name;

    /**
     * 월 금액 (원)
     */
    @NotNull(message = "월 소득 금액은 필수입니다")
    @PositiveOrZero(message = "월 소득 금액은 0 이상이어야 합니다")
    private Long amount;

    /**
     * 도메인 객체로 변환
     *
     * @return IncomeItem 도메인 객체
     */
    public IncomeItem toDomain() {
        return IncomeItem.builder()
                .id(id != null ? id : UUID.randomUUID().toString())
                .name(name)
                .amount(amount)
                .build();
    }

    /**
     * 도메인 객체로부터 DTO 생성
     *
     * @param item IncomeItem 도메인 객체
     * @return IncomeItemDto
     */
    public static IncomeItemDto fromDomain(IncomeItem item) {
        return IncomeItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .amount(item.getAmount())
                .build();
    }
}
