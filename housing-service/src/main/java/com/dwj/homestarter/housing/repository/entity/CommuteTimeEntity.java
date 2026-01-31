package com.dwj.homestarter.housing.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 출퇴근 시간 엔티티
 * 교통호재별 출퇴근 소요시간 정보
 */
@Entity
@Table(name = "commute_times")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommuteTimeEntity {

    /**
     * 출퇴근시간 ID (기본키)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commute_time_id")
    private Long id;

    /**
     * 교통호재 참조 (1:1 관계)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportation_id", nullable = false, unique = true)
    private TransportationEntity transportation;

    /**
     * 본인 출근 소요시간 (9시 이전 도착, 분)
     */
    @Column(name = "self_before_9am")
    private Integer selfBefore9am;

    /**
     * 본인 퇴근 소요시간 (18시 이후 출발, 분)
     */
    @Column(name = "self_after_6pm")
    private Integer selfAfter6pm;

    /**
     * 배우자 출근 소요시간 (9시 이전 도착, 분)
     */
    @Column(name = "spouse_before_9am")
    private Integer spouseBefore9am;

    /**
     * 배우자 퇴근 소요시간 (18시 이후 출발, 분)
     */
    @Column(name = "spouse_after_6pm")
    private Integer spouseAfter6pm;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Transportation 엔티티 설정
     */
    public void setTransportation(TransportationEntity transportation) {
        this.transportation = transportation;
    }
}
