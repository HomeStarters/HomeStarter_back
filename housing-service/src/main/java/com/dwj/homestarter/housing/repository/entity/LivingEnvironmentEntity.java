package com.dwj.homestarter.housing.repository.entity;

import com.dwj.homestarter.housing.domain.enums.NoiseLevel;
import com.dwj.homestarter.housing.domain.enums.SunlightLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 생활환경 정보 엔티티
 * 주택의 생활환경 상세 정보를 저장
 */
@Entity
@Table(name = "living_environments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LivingEnvironmentEntity {

    /**
     * 생활환경 정보 ID (기본키)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "living_environment_id")
    private Long id;

    /**
     * 주택 엔티티 (1:1 관계)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "housing_id", nullable = false, unique = true)
    private HousingEntity housing;

    /**
     * 일조량 수준
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sunlight_level", length = 20)
    private SunlightLevel sunlightLevel;

    /**
     * 소음 수준
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "noise_level", length = 20)
    private NoiseLevel noiseLevel;

    /**
     * 인근 학교 목록 (JSON 배열로 저장)
     */
    @ElementCollection
    @CollectionTable(
            name = "living_environment_schools",
            joinColumns = @JoinColumn(name = "living_environment_id")
    )
    @Column(name = "school_name")
    @Builder.Default
    private List<String> nearbySchools = new ArrayList<>();

    /**
     * 인근 마트 목록 (JSON 배열로 저장)
     */
    @ElementCollection
    @CollectionTable(
            name = "living_environment_marts",
            joinColumns = @JoinColumn(name = "living_environment_id")
    )
    @Column(name = "mart_name")
    @Builder.Default
    private List<String> nearbyMarts = new ArrayList<>();

    /**
     * 인근 병원 목록 (JSON 배열로 저장)
     */
    @ElementCollection
    @CollectionTable(
            name = "living_environment_hospitals",
            joinColumns = @JoinColumn(name = "living_environment_id")
    )
    @Column(name = "hospital_name")
    @Builder.Default
    private List<String> nearbyHospitals = new ArrayList<>();

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
     * 생활환경 정보 업데이트
     * 기존 엔티티의 필드 값을 새 값으로 업데이트
     */
    public void updateInfo(SunlightLevel sunlightLevel, NoiseLevel noiseLevel,
                           List<String> nearbySchools, List<String> nearbyMarts,
                           List<String> nearbyHospitals) {
        this.sunlightLevel = sunlightLevel;
        this.noiseLevel = noiseLevel;
        this.nearbySchools.clear();
        if (nearbySchools != null) {
            this.nearbySchools.addAll(nearbySchools);
        }
        this.nearbyMarts.clear();
        if (nearbyMarts != null) {
            this.nearbyMarts.addAll(nearbyMarts);
        }
        this.nearbyHospitals.clear();
        if (nearbyHospitals != null) {
            this.nearbyHospitals.addAll(nearbyHospitals);
        }
    }
}
