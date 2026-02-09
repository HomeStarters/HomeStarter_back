-- 지역 특성 마스터 데이터 초기화
-- 이미 데이터가 존재하면 건너뜀 (INSERT ON CONFLICT)

INSERT INTO housing_service.regional_characteristics (region_code, region_description, ltv, dti, created_at)
VALUES ('LTPZ', '토지거래허가구역', 0.4000, 0.4000, CURRENT_TIMESTAMP)
ON CONFLICT (region_code) DO NOTHING;

INSERT INTO housing_service.regional_characteristics (region_code, region_description, ltv, dti, created_at)
VALUES ('OHA', '투기과열지구', 0.4000, 0.4000, CURRENT_TIMESTAMP)
ON CONFLICT (region_code) DO NOTHING;

INSERT INTO housing_service.regional_characteristics (region_code, region_description, ltv, dti, created_at)
VALUES ('AA', '조정대상지역', 0.5000, 0.5000, CURRENT_TIMESTAMP)
ON CONFLICT (region_code) DO NOTHING;

INSERT INTO housing_service.regional_characteristics (region_code, region_description, ltv, dti, created_at)
VALUES ('G', '일반지역', 0.7000, 0.6000, CURRENT_TIMESTAMP)
ON CONFLICT (region_code) DO NOTHING;
