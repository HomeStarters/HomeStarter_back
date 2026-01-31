package com.dwj.homestarter.user.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 임베디드 타입
 *
 * 주소 정보를 포함하는 임베디드 타입
 *
 * @author homestarter
 * @since 1.0.0
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressEmbeddable {

    /**
     * 도로명 주소
     */
    @Column(name = "road_address", length = 200)
    private String roadAddress;

    /**
     * 지번 주소
     */
    @Column(name = "jibun_address", length = 200)
    private String jibunAddress;

    /**
     * 우편번호
     */
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    /**
     * 위도
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * 경도
     */
    @Column(name = "longitude")
    private Double longitude;
}
