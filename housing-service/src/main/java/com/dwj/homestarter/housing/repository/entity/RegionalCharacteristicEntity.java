package com.dwj.homestarter.housing.repository.entity;

import com.dwj.homestarter.housing.domain.enums.RegionCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 특수 지역별 특성 기준 엔티티
 * 지역별 LTV, DTI 정보를 관리하는 마스터 데이터
 */
@Entity
@Table(name = "regional_characteristics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RegionalCharacteristicEntity {

    /**
     * 지역 코드 (기본키)
     */
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "region_code", length = 10)
    private RegionCode regionCode;

    /**
     * 지역 설명
     */
    @Column(name = "region_description", nullable = false, length = 50)
    private String regionDescription;

    /**
     * LTV (담보인정비율)
     * 0.0 ~ 1.0 범위
     */
    @Column(name = "ltv", nullable = false, precision = 5, scale = 4)
    private BigDecimal ltv;

    /**
     * DTI (총부채상환비율)
     * 0.0 ~ 1.0 범위
     */
    @Column(name = "dti", nullable = false, precision = 5, scale = 4)
    private BigDecimal dti;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
