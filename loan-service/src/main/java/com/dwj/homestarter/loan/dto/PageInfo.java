package com.dwj.homestarter.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * 페이징 정보 DTO
 *
 * 페이지 메타데이터 전송 객체
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageInfo {

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int pageNumber;

    /**
     * 페이지 크기
     */
    private int pageSize;

    /**
     * 전체 요소 수
     */
    private long totalElements;

    /**
     * 전체 페이지 수
     */
    private int totalPages;

    /**
     * Page 객체를 PageInfo로 변환
     *
     * @param page Spring Data Page 객체
     * @return PageInfo
     */
    public static PageInfo from(Page<?> page) {
        if (page == null) {
            return null;
        }
        return PageInfo.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
