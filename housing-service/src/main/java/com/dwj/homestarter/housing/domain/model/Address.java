package com.dwj.homestarter.housing.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 주소 값 객체
 * 불변 객체로 주소 정보를 표현
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address {

    /**
     * 전체 주소
     */
    @Column(name = "full_address", nullable = false, columnDefinition = "TEXT")
    private String fullAddress;

    /**
     * 도로명 주소
     */
    @Column(name = "road_address", columnDefinition = "TEXT")
    private String roadAddress;

    /**
     * 지번 주소
     */
    @Column(name = "jibun_address", columnDefinition = "TEXT")
    private String jibunAddress;

    /**
     * 위도
     */
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    /**
     * 경도
     */
    @Column(name = "longitude", precision = 11, scale = 7)
    private BigDecimal longitude;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(fullAddress, address.fullAddress) &&
               Objects.equals(latitude, address.latitude) &&
               Objects.equals(longitude, address.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullAddress, latitude, longitude);
    }
}
