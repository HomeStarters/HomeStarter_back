package com.dwj.homestarter.asset.repository.entity;

import com.dwj.homestarter.asset.domain.AssetItem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 자산 항목 JPA 엔티티
 * 예금, 적금, 주식 등 자산 상세 항목 저장
 */
@Entity
@Table(name = "asset_items", schema = "asset_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetItemEntity {

    /**
     * 자산 항목 ID (UUID)
     */
    @Id
    @Column(name = "id", length = 50)
    private String id;

    /**
     * 자산정보 ID (FK)
     */
    @Column(name = "asset_id", nullable = false, length = 50)
    private String assetId;

    /**
     * 자산명 (예: 국민은행 예금)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 금액 (원)
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 도메인 객체로 변환
     *
     * @return AssetItem 도메인 객체
     */
    public AssetItem toDomain() {
        return AssetItem.builder()
                .id(this.id)
                .name(this.name)
                .amount(this.amount)
                .build();
    }

    /**
     * 도메인 객체로부터 엔티티 생성
     *
     * @param assetId 자산정보 ID
     * @param item    AssetItem 도메인 객체
     * @return AssetItemEntity
     */
    public static AssetItemEntity fromDomain(String assetId, AssetItem item) {
        return AssetItemEntity.builder()
                .id(item.getId())
                .assetId(assetId)
                .name(item.getName())
                .amount(item.getAmount())
                .build();
    }
}
