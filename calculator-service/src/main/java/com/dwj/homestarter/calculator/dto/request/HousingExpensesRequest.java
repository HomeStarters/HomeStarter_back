package com.dwj.homestarter.calculator.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 입주 후 지출 계산 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "입주 후 지출 계산 요청")
public class HousingExpensesRequest {

    /**
     * 주택 ID
     */
    @NotBlank(message = "주택 ID는 필수입니다")
    @Schema(description = "주택 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private String housingId;

    /**
     * 대출상품 ID
     */
    @NotBlank(message = "대출상품 ID는 필수입니다")
    @Schema(description = "대출상품 ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private String loanProductId;

    /**
     * 대출 금액 (원) - 선택사항
     * useLoanRequiredAsLoanAmount가 true인 경우 미입력 가능
     */
    @Min(value = 0, message = "대출 금액은 0 이상이어야 합니다")
    @Schema(description = "대출 금액 (원, 선택사항)", example = "300000000")
    private Long loanAmount;

    /**
     * 대출 기간 (개월)
     */
    @NotNull(message = "대출 기간은 필수입니다")
    @Min(value = 1, message = "대출 기간은 1개월 이상이어야 합니다")
    @Schema(description = "대출 기간 (개월)", example = "360")
    private Integer loanTerm;

    /**
     * 지출 계산에 포함할 가구원 ID 리스트 (선택사항)
     * null 또는 빈 리스트인 경우 본인만으로 계산
     */
    @Schema(description = "지출 계산에 포함할 가구원 ID 리스트", example = "[\"member-user-id-1\", \"member-user-id-2\"]")
    private List<String> householdMemberIds;

    /**
     * 대출필요금액을 대출금액으로 산정하여 계산 여부
     * true인 경우 계산된 대출필요금액(loanRequired)을 대출금액(loanAmount)으로 대체하여 이후 계산 수행
     */
    @Schema(description = "대출필요금액으로 대출금액 산정 여부 (true: 대출필요금액을 대출금액으로 사용)", example = "false")
    @Builder.Default
    private Boolean useLoanRequiredAsLoanAmount = false;
}
