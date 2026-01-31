package com.dwj.homestarter.housing.repository.entity;

import com.dwj.homestarter.housing.domain.enums.TransportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 교통호재 엔티티
 * 주택별 교통 편의 정보 (지하철, 버스, 기차 등)
 */
@Entity
@Table(name = "transportations",
        indexes = {
            @Index(name = "idx_housing_id", columnList = "housing_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TransportationEntity {

    /**
     * 교통호재 ID (기본키)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transportation_id")
    private Long id;

    /**
     * 주택 참조 (N:1 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "housing_id", nullable = false)
    private HousingEntity housing;

    /**
     * 교통수단 유형 (지하철, 버스, 기차)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 50)
    private TransportType transportType;

    /**
     * 노선명 (예: 2호선, 9호선)
     */
    @Column(name = "line_name", length = 100)
    private String lineName;

    /**
     * 역/정류장명
     */
    @Column(name = "station_name", nullable = false, length = 200)
    private String stationName;

    /**
     * 거리 (m)
     */
    @Column(name = "distance", precision = 6, scale = 2)
    private BigDecimal distance;

    /**
     * 도보 소요시간 (분)
     */
    @Column(name = "walking_time")
    private Integer walkingTime;

    /**
     * 출퇴근 시간 정보 (1:1 관계)
     */
    @OneToOne(mappedBy = "transportation", cascade = CascadeType.ALL, orphanRemoval = true)
    private CommuteTimeEntity commuteTime;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Housing 엔티티 설정
     */
    public void setHousing(HousingEntity housing) {
        this.housing = housing;
    }

    /**
     * CommuteTime 양방향 연관관계 설정
     */
    public void setCommuteTime(CommuteTimeEntity commuteTime) {
        this.commuteTime = commuteTime;
        if (commuteTime != null) {
            commuteTime.setTransportation(this);
        }
    }
}
