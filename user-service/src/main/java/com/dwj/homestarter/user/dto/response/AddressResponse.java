package com.dwj.homestarter.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 응답 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    /**
     * 도로명 주소
     */
    private String roadAddress;

    /**
     * 지번 주소
     */
    private String jibunAddress;

    /**
     * 우편번호
     */
    private String postalCode;

    /**
     * 위도
     */
    private Double latitude;

    /**
     * 경도
     */
    private Double longitude;
}
