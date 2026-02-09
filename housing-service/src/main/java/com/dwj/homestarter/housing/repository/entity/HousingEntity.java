package com.dwj.homestarter.housing.repository.entity;

import com.dwj.homestarter.housing.domain.enums.HousingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주택 정보 엔티티
 * 사용자가 등록한 주택의 상세 정보
 */
@Entity
@Table(name = "housings",
        indexes = {
            @Index(name = "idx_user_id", columnList = "user_id"),
            @Index(name = "idx_user_goal", columnList = "user_id, is_goal"),
            @Index(name = "idx_region_code", columnList = "region_code")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HousingEntity {

    /**
     * 주택 ID (기본키)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "housing_id")
    private Long id;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * 주택명
     */
    @Column(name = "housing_name", nullable = false, length = 200)
    private String housingName;

    /**
     * 주택 유형 (아파트, 오피스텔, 빌라, 단독/다가구)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type", nullable = false, length = 50)
    private HousingType housingType;

    /**
     * 가격 (원)
     */
    @Column(name = "price", nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    /**
     * 입주희망년월 (YYYY-MM)
     */
    @Column(name = "move_in_date", length = 7)
    private String moveInDate;

    /**
     * 준공일
     */
    @Column(name = "completion_date")
    private LocalDate completionDate;

    /**
     * 주소 정보
     */
    @Column(name = "address")
    private String address;

    /**
     * 지역 특성 정보 (N:1 관계)
     * Housing 삭제 시 지역특성은 영향 없음 (마스터 데이터)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", referencedColumnName = "region_code")
    private RegionalCharacteristicEntity regionalCharacteristic;

    /**
     * 단지 정보 (1:1 관계)
     */
    @OneToOne(mappedBy = "housing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ComplexInfoEntity complexInfo;

    /**
     * 생활환경 정보 (1:1 관계)
     */
    @OneToOne(mappedBy = "housing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LivingEnvironmentEntity livingEnvironment;

    /**
     * 최종목표 주택 여부
     */
    @Column(name = "is_goal", nullable = false)
    @Builder.Default
    private Boolean isGoal = false;

    /**
     * 교통호재 목록 (1:N 관계)
     */
    @OneToMany(mappedBy = "housing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransportationEntity> transportations = new ArrayList<>();

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 최종목표 주택 설정
     */
    public void setAsGoal() {
        this.isGoal = true;
    }

    /**
     * 최종목표 주택 해제
     */
    public void unsetAsGoal() {
        this.isGoal = false;
    }

    /**
     * 교통호재 추가 (양방향 연관관계 설정)
     */
    public void addTransportation(TransportationEntity transportation) {
        transportations.add(transportation);
        transportation.setHousing(this);
    }

    /**
     * 교통호재 제거 (양방향 연관관계 해제)
     */
    public void removeTransportation(TransportationEntity transportation) {
        transportations.remove(transportation);
        transportation.setHousing(null);
    }

    /**
     * 모든 교통호재 제거
     * 양방향 연관관계를 올바르게 해제하기 위해 각 항목의 housing 참조를 null로 설정
     */
    public void clearTransportations() {
        transportations.forEach(t -> t.setHousing(null));
        transportations.clear();
    }

    /**
     * 단지 정보 설정 (양방향 연관관계)
     */
    public void setComplexInfo(ComplexInfoEntity complexInfo) {
        this.complexInfo = complexInfo;
        if (complexInfo != null) {
            complexInfo.setHousing(this);
        }
    }

    /**
     * 생활환경 정보 설정 (양방향 연관관계)
     */
    public void setLivingEnvironment(LivingEnvironmentEntity livingEnvironment) {
        this.livingEnvironment = livingEnvironment;
        if (livingEnvironment != null) {
            livingEnvironment.setHousing(this);
        }
    }

    /**
     * 주택 정보 업데이트
     */
    public void updateHousingInfo(String housingName, HousingType housingType, BigDecimal price,
                                  String moveInDate, LocalDate completionDate, String address) {
        this.housingName = housingName;
        this.housingType = housingType;
        this.price = price;
        this.moveInDate = moveInDate;
        this.completionDate = completionDate;
        this.address = address;
    }

    /**
     * 지역 특성 정보 설정
     */
    public void setRegionalCharacteristic(RegionalCharacteristicEntity regionalCharacteristic) {
        this.regionalCharacteristic = regionalCharacteristic;
    }

    /**
     * 단지 정보 업데이트
     */
    public void updateComplexInfo(ComplexInfoEntity newComplexInfo) {
        if (this.complexInfo != null) {
            this.complexInfo.setHousing(null);
        }
        this.complexInfo = newComplexInfo;
        if (newComplexInfo != null) {
            newComplexInfo.setHousing(this);
        }
    }

    /**
     * 생활환경 정보 업데이트
     */
    public void updateLivingEnvironment(LivingEnvironmentEntity newLivingEnvironment) {
        if (this.livingEnvironment != null) {
            this.livingEnvironment.setHousing(null);
        }
        this.livingEnvironment = newLivingEnvironment;
        if (newLivingEnvironment != null) {
            newLivingEnvironment.setHousing(this);
        }
    }
}
