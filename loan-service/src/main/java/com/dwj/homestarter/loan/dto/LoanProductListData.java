package com.dwj.homestarter.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대출상품 목록 데이터 DTO
 *
 * 대출상품 목록과 페이징 정보를 포함하는 데이터 객체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductListData {

    /**
     * 대출상품 목록
     */
    private List<LoanProductDTO> content;

    /**
     * 페이징 정보
     */
    private PageInfo pageable;
}
