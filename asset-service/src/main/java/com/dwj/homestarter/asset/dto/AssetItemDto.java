package com.dwj.homestarter.asset.dto;

import com.dwj.homestarter.asset.domain.AssetItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 자산 항목 DTO
 * 자산 항목 정보 전달용 데이터 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetItemDto {

    /**
     * 자산 항목 ID (응답 시에만 사용)
     */
    private String id;

    /**
     * 자산명 (예: 국민은행 예금)
     */
    @NotBlank(message = "자산명은 필수입니다")
    private String name;

    /**
     * 금액 (원)
     */
    @NotNull(message = "금액은 필수입니다")
    @PositiveOrZero(message = "금액은 0 이상이어야 합니다")
    private Long amount;

    /**
     * 도메인 객체로 변환
     *
     * @return AssetItem 도메인 객체
     */
    public AssetItem toDomain() {
        return AssetItem.builder()
                .id(id != null ? id : UUID.randomUUID().toString())
                .name(name)
                .amount(amount)
                .build();
    }

    /**
     * 도메인 객체로부터 DTO 생성
     *
     * @param item AssetItem 도메인 객체
     * @return AssetItemDto
     */
    public static AssetItemDto fromDomain(AssetItem item) {
        return AssetItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .amount(item.getAmount())
                .build();
    }
}
