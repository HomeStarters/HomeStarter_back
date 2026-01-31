package com.dwj.homestarter.housing.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 단지 정보 엔티티
 * 주택의 단지 상세 정보를 저장
 */
@Entity
@Table(name = "complex_infos")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ComplexInfoEntity {

    /**
     * 단지 정보 ID (기본키)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complex_info_id")
    private Long id;

    /**
     * 주택 엔티티 (1:1 관계)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "housing_id", nullable = false, unique = true)
    private HousingEntity housing;

    /**
     * 단지명
     */
    @Column(name = "complex_name", length = 200)
    private String complexName;

    /**
     * 총 세대수
     */
    @Column(name = "total_households")
    private Integer totalHouseholds;

    /**
     * 총 동수
     */
    @Column(name = "total_dong")
    private Integer totalDong;

    /**
     * 총 층수
     */
    @Column(name = "total_floors")
    private Integer totalFloors;

    /**
     * 주차 대수
     */
    @Column(name = "parking_count")
    private Integer parkingCount;

    /**
     * 입주년월 (YYYY-MM)
     */
    @Column(name = "move_in_date", length = 7)
    private String moveInDate;

    /**
     * 건설사
     */
    @Column(name = "construction_company", length = 100)
    private String constructionCompany;

    /**
     * 공급면적 (㎡)
     */
    @Column(name = "house_area", precision = 10, scale = 2)
    private BigDecimal houseArea;

    /**
     * 전용면적 (㎡)
     */
    @Column(name = "exclusive_area", precision = 10, scale = 2)
    private BigDecimal exclusiveArea;

    /**
     * 층수
     */
    @Column(name = "floor")
    private Integer floor;

    /**
     * 향
     */
    @Column(name = "direction", length = 50)
    private String direction;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Housing 설정 (양방향 연관관계)
     */
    public void setHousing(HousingEntity housing) {
        this.housing = housing;
    }

    /**
     * 단지 정보 업데이트
     * 기존 엔티티의 필드 값을 새 값으로 업데이트
     */
    public void updateInfo(String complexName, Integer totalHouseholds, Integer totalDong,
                           Integer totalFloors, Integer parkingCount, String moveInDate,
                           String constructionCompany, BigDecimal houseArea, BigDecimal exclusiveArea,
                           Integer floor, String direction) {
        this.complexName = complexName;
        this.totalHouseholds = totalHouseholds;
        this.totalDong = totalDong;
        this.totalFloors = totalFloors;
        this.parkingCount = parkingCount;
        this.moveInDate = moveInDate;
        this.constructionCompany = constructionCompany;
        this.houseArea = houseArea;
        this.exclusiveArea = exclusiveArea;
        this.floor = floor;
        this.direction = direction;
    }
}
