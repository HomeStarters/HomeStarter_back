package com.dwj.homestarter.calculator.domain;

import com.dwj.homestarter.calculator.dto.external.AssetDto;
import com.dwj.homestarter.calculator.dto.external.HousingDto;
import com.dwj.homestarter.calculator.dto.external.LoanProductDto;
import com.dwj.homestarter.calculator.dto.external.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 외부 서비스 데이터 번들
 * 외부 서비스로부터 수집한 데이터를 담는 컨테이너
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalDataBundle {

    /**
     * 사용자 프로필 정보
     */
    private UserProfileDto user;

    /**
     * 자산 정보
     */
    private AssetDto asset;

    /**
     * 주택 정보
     */
    private HousingDto housing;

    /**
     * 대출상품 정보
     */
    private LoanProductDto loan;
}
