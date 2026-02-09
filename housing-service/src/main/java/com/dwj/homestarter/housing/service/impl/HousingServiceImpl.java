package com.dwj.homestarter.housing.service.impl;

import com.dwj.homestarter.common.exception.NotFoundException;
import com.dwj.homestarter.housing.domain.model.Address;
import com.dwj.homestarter.housing.domain.model.ComplexInfo;
import com.dwj.homestarter.housing.domain.model.LivingEnvironment;
import com.dwj.homestarter.housing.dto.request.*;
import com.dwj.homestarter.housing.dto.response.*;
import com.dwj.homestarter.housing.dto.response.HousingDeleteResponse;
import com.dwj.homestarter.housing.repository.entity.*;
import com.dwj.homestarter.housing.repository.jpa.HousingRepository;
import com.dwj.homestarter.housing.repository.jpa.RegionalCharacteristicRepository;
import com.dwj.homestarter.housing.service.HousingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주택 서비스 구현체
 * 주택 관리 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HousingServiceImpl implements HousingService {

    private final HousingRepository housingRepository;
    private final RegionalCharacteristicRepository regionalCharacteristicRepository;

    @Override
    @Transactional
    public HousingResponse createHousing(String userId, HousingCreateRequest request) {
        log.info("주택 생성 요청: userId={}, housingName={}", userId, request.getHousingName());

        // 지역 특성 조회 (선택적)
        RegionalCharacteristicEntity regionalCharacteristic = null;
        if (request.getRegionCode() != null) {
            regionalCharacteristic = regionalCharacteristicRepository
                    .findByRegionCode(request.getRegionCode())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "유효하지 않은 지역코드입니다: " + request.getRegionCode()));
        }

        // Entity 변환 및 저장
        HousingEntity housing = HousingEntity.builder()
                .userId(userId)
                .housingName(request.getHousingName())
                .housingType(request.getHousingType())
                .price(request.getPrice())
                .moveInDate(request.getMoveInDate())
                .completionDate(request.getCompletionDate())
                .address(request.getAddress())
                .regionalCharacteristic(regionalCharacteristic)
                .isGoal(false)
                .transportations(new ArrayList<>())
                .build();

        // 단지 정보 설정 (1:1 관계)
        if (request.getComplexInfo() != null) {
            ComplexInfoEntity complexInfoEntity = convertToComplexInfoEntity(request.getComplexInfo());
            housing.setComplexInfo(complexInfoEntity);
        }

        // 생활환경 정보 설정 (1:1 관계)
        if (request.getLivingEnvironment() != null) {
            LivingEnvironmentEntity livingEnvironmentEntity = convertToLivingEnvironmentEntity(request.getLivingEnvironment());
            housing.setLivingEnvironment(livingEnvironmentEntity);
        }

        // 교통호재 추가
        if (request.getTransportations() != null) {
            request.getTransportations().forEach(tr -> {
                TransportationEntity transportation = convertToTransportationEntity(tr);
                housing.addTransportation(transportation);
            });
        }

        HousingEntity saved = housingRepository.save(housing);
        log.info("주택 생성 완료: housingId={}", saved.getId());

        return convertToHousingResponse(saved);
    }

    @Override
    public HousingListResponse getHousings(String userId, Pageable pageable) {
        log.info("주택 목록 조회: userId={}, page={}", userId, pageable.getPageNumber());

        Page<HousingEntity> page = housingRepository.findByUserId(userId, pageable);

        List<HousingListItem> items = page.getContent().stream()
                .map(this::convertToHousingListItem)
                .collect(Collectors.toList());

        return HousingListResponse.builder()
                .housings(items)
                .totalCount(page.getTotalElements())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public HousingResponse getHousing(Long housingId, String userId) {
        log.info("주택 상세 조회: housingId={}, userId={}", housingId, userId);

        HousingEntity housing = housingRepository.findByIdAndUserId(housingId, userId)
                .orElseThrow(() -> new NotFoundException("주택을 찾을 수 없습니다"));

        return convertToHousingResponse(housing);
    }

    @Override
    @Transactional
    public HousingResponse updateHousing(Long housingId, String userId, HousingUpdateRequest request) {
        log.info("주택 정보 수정: housingId={}, userId={}", housingId, userId);

        HousingEntity housing = housingRepository.findByIdAndUserId(housingId, userId)
                .orElseThrow(() -> new NotFoundException("주택을 찾을 수 없습니다"));

        // 주택 기본 정보 업데이트
        housing.updateHousingInfo(
                request.getHousingName(),
                request.getHousingType(),
                request.getPrice(),
                request.getMoveInDate(),
                request.getCompletionDate(),
                request.getAddress()
        );

        // 지역 특성 업데이트
        if (request.getRegionCode() != null) {
            RegionalCharacteristicEntity regionalCharacteristic = regionalCharacteristicRepository
                    .findByRegionCode(request.getRegionCode())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "유효하지 않은 지역코드입니다: " + request.getRegionCode()));
            housing.setRegionalCharacteristic(regionalCharacteristic);
        } else {
            housing.setRegionalCharacteristic(null);
        }

        // 단지 정보 업데이트 (1:1 관계) - 기존 데이터 유무 확인 후 처리
        if (request.getComplexInfo() != null) {
            ComplexInfoEntity existingComplexInfo = housing.getComplexInfo();
            if (existingComplexInfo != null) {
                // 기존 데이터가 있으면 필드 값만 업데이트
                existingComplexInfo.updateInfo(
                        request.getComplexInfo().getComplexName(),
                        request.getComplexInfo().getTotalHouseholds(),
                        request.getComplexInfo().getTotalDong(),
                        request.getComplexInfo().getTotalFloors(),
                        request.getComplexInfo().getParkingCount(),
                        request.getComplexInfo().getMoveInDate(),
                        request.getComplexInfo().getConstructionCompany(),
                        request.getComplexInfo().getHouseArea(),
                        request.getComplexInfo().getExclusiveArea(),
                        request.getComplexInfo().getFloor(),
                        request.getComplexInfo().getDirection()
                );
            } else {
                // 기존 데이터가 없으면 새 엔티티 생성
                ComplexInfoEntity complexInfoEntity = convertToComplexInfoEntity(request.getComplexInfo());
                housing.setComplexInfo(complexInfoEntity);
            }
        } else {
            housing.updateComplexInfo(null);
        }

        // 생활환경 정보 업데이트 (1:1 관계) - 기존 데이터 유무 확인 후 처리
        if (request.getLivingEnvironment() != null) {
            LivingEnvironmentEntity existingLivingEnv = housing.getLivingEnvironment();
            if (existingLivingEnv != null) {
                // 기존 데이터가 있으면 필드 값만 업데이트
                existingLivingEnv.updateInfo(
                        request.getLivingEnvironment().getSunlightLevel(),
                        request.getLivingEnvironment().getNoiseLevel(),
                        request.getLivingEnvironment().getNearbySchools(),
                        request.getLivingEnvironment().getNearbyMarts(),
                        request.getLivingEnvironment().getNearbyHospitals()
                );
            } else {
                // 기존 데이터가 없으면 새 엔티티 생성
                LivingEnvironmentEntity livingEnvironmentEntity = convertToLivingEnvironmentEntity(request.getLivingEnvironment());
                housing.setLivingEnvironment(livingEnvironmentEntity);
            }
        } else {
            housing.updateLivingEnvironment(null);
        }

        // 교통호재 업데이트 (기존 삭제 후 새로 추가)
        housing.clearTransportations();
        if (request.getTransportations() != null) {
            request.getTransportations().forEach(tr -> {
                TransportationEntity transportation = convertToTransportationEntity(tr);
                housing.addTransportation(transportation);
            });
        }

        log.info("주택 정보 수정 완료: housingId={}", housingId);
        return convertToHousingResponse(housing);
    }

    @Override
    @Transactional
    public HousingDeleteResponse deleteHousing(Long housingId, String userId) {
        log.info("주택 삭제: housingId={}, userId={}", housingId, userId);

        HousingEntity housing = housingRepository.findByIdAndUserId(housingId, userId)
                .orElseThrow(() -> new NotFoundException("주택을 찾을 수 없습니다"));

        String housingName = housing.getHousingName();
        housingRepository.delete(housing);
        log.info("주택 삭제 완료: housingId={}", housingId);

        return HousingDeleteResponse.builder()
                .housingId(housingId)
                .housingName(housingName)
                .message("주택이 삭제되었습니다")
                .build();
    }

    @Override
    @Transactional
    public GoalHousingResponse setGoalHousing(Long housingId, String userId) {
        log.info("최종목표 주택 설정: housingId={}, userId={}", housingId, userId);

        HousingEntity housing = housingRepository.findByIdAndUserId(housingId, userId)
                .orElseThrow(() -> new NotFoundException("주택을 찾을 수 없습니다"));

        // 기존 최종목표 주택 해제
        housingRepository.clearGoalHousingByUserId(userId);

        // 새로운 최종목표 설정
        housing.setAsGoal();

        log.info("최종목표 주택 설정 완료: housingId={}", housingId);

        return GoalHousingResponse.builder()
                .housingId(housing.getId())
                .housingName(housing.getHousingName())
                .message("최종목표 주택으로 설정되었습니다")
                .build();
    }

    // === Private Helper Methods ===

    private Address convertToAddress(AddressRequest request) {
        return Address.builder()
                .fullAddress(request.getFullAddress())
                .roadAddress(request.getRoadAddress())
                .jibunAddress(request.getJibunAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    /**
     * ComplexInfo 도메인 모델 → ComplexInfoEntity 변환
     */
    private ComplexInfoEntity convertToComplexInfoEntity(ComplexInfo complexInfo) {
        return ComplexInfoEntity.builder()
                .complexName(complexInfo.getComplexName())
                .totalHouseholds(complexInfo.getTotalHouseholds())
                .totalDong(complexInfo.getTotalDong())
                .totalFloors(complexInfo.getTotalFloors())
                .parkingCount(complexInfo.getParkingCount())
                .moveInDate(complexInfo.getMoveInDate())
                .constructionCompany(complexInfo.getConstructionCompany())
                .houseArea(complexInfo.getHouseArea())
                .exclusiveArea(complexInfo.getExclusiveArea())
                .floor(complexInfo.getFloor())
                .direction(complexInfo.getDirection())
                .build();
    }

    /**
     * LivingEnvironment 도메인 모델 → LivingEnvironmentEntity 변환
     */
    private LivingEnvironmentEntity convertToLivingEnvironmentEntity(LivingEnvironment livingEnvironment) {
        return LivingEnvironmentEntity.builder()
                .sunlightLevel(livingEnvironment.getSunlightLevel())
                .noiseLevel(livingEnvironment.getNoiseLevel())
                .nearbySchools(livingEnvironment.getNearbySchools() != null ?
                        new ArrayList<>(livingEnvironment.getNearbySchools()) : new ArrayList<>())
                .nearbyMarts(livingEnvironment.getNearbyMarts() != null ?
                        new ArrayList<>(livingEnvironment.getNearbyMarts()) : new ArrayList<>())
                .nearbyHospitals(livingEnvironment.getNearbyHospitals() != null ?
                        new ArrayList<>(livingEnvironment.getNearbyHospitals()) : new ArrayList<>())
                .build();
    }

    /**
     * ComplexInfoEntity → ComplexInfo 도메인 모델 변환
     */
    private ComplexInfo convertToComplexInfo(ComplexInfoEntity entity) {
        if (entity == null) {
            return null;
        }
        return ComplexInfo.builder()
                .complexName(entity.getComplexName())
                .totalHouseholds(entity.getTotalHouseholds())
                .totalDong(entity.getTotalDong())
                .totalFloors(entity.getTotalFloors())
                .parkingCount(entity.getParkingCount())
                .moveInDate(entity.getMoveInDate())
                .constructionCompany(entity.getConstructionCompany())
                .houseArea(entity.getHouseArea())
                .exclusiveArea(entity.getExclusiveArea())
                .floor(entity.getFloor())
                .direction(entity.getDirection())
                .build();
    }

    /**
     * LivingEnvironmentEntity → LivingEnvironment 도메인 모델 변환
     */
    private LivingEnvironment convertToLivingEnvironment(LivingEnvironmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return LivingEnvironment.builder()
                .sunlightLevel(entity.getSunlightLevel())
                .noiseLevel(entity.getNoiseLevel())
                .nearbySchools(entity.getNearbySchools() != null ?
                        new ArrayList<>(entity.getNearbySchools()) : new ArrayList<>())
                .nearbyMarts(entity.getNearbyMarts() != null ?
                        new ArrayList<>(entity.getNearbyMarts()) : new ArrayList<>())
                .nearbyHospitals(entity.getNearbyHospitals() != null ?
                        new ArrayList<>(entity.getNearbyHospitals()) : new ArrayList<>())
                .build();
    }

    private TransportationEntity convertToTransportationEntity(TransportationRequest request) {
        TransportationEntity transportation = TransportationEntity.builder()
                .transportType(request.getTransportType())
                .lineName(request.getLineName())
                .stationName(request.getStationName())
                .distance(request.getDistance())
                .walkingTime(request.getWalkingTime())
                .build();

        if (request.getCommuteTime() != null) {
            CommuteTimeEntity commuteTime = CommuteTimeEntity.builder()
                    .selfBefore9am(request.getCommuteTime().getSelfBefore9am())
                    .selfAfter6pm(request.getCommuteTime().getSelfAfter6pm())
                    .spouseBefore9am(request.getCommuteTime().getSpouseBefore9am())
                    .spouseAfter6pm(request.getCommuteTime().getSpouseAfter6pm())
                    .build();
            transportation.setCommuteTime(commuteTime);
        }

        return transportation;
    }

    private HousingResponse convertToHousingResponse(HousingEntity entity) {
        return HousingResponse.builder()
                .id(entity.getId())
                .housingName(entity.getHousingName())
                .housingType(entity.getHousingType())
                .price(entity.getPrice())
                .moveInDate(entity.getMoveInDate())
                .completionDate(entity.getCompletionDate())
                .address(entity.getAddress())
                .regionalCharacteristic(convertToRegionalCharacteristicResponse(entity.getRegionalCharacteristic()))
                .complexInfo(convertToComplexInfo(entity.getComplexInfo()))
                .livingEnvironment(convertToLivingEnvironment(entity.getLivingEnvironment()))
                .isGoal(entity.getIsGoal())
                .transportations(entity.getTransportations().stream()
                        .map(this::convertToTransportationResponse)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private HousingListItem convertToHousingListItem(HousingEntity entity) {
        RegionalCharacteristicEntity regionalChar = entity.getRegionalCharacteristic();
        return HousingListItem.builder()
                .id(entity.getId())
                .housingName(entity.getHousingName())
                .housingType(entity.getHousingType())
                .price(entity.getPrice())
                .fullAddress(entity.getAddress())
                .regionCode(regionalChar != null ? regionalChar.getRegionCode() : null)
                .regionDescription(regionalChar != null ? regionalChar.getRegionDescription() : null)
                .isGoal(entity.getIsGoal())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private AddressResponse convertToAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponse.builder()
                .fullAddress(address.getFullAddress())
                .roadAddress(address.getRoadAddress())
                .jibunAddress(address.getJibunAddress())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    private TransportationResponse convertToTransportationResponse(TransportationEntity entity) {
        return TransportationResponse.builder()
                .id(entity.getId())
                .transportType(entity.getTransportType())
                .lineName(entity.getLineName())
                .stationName(entity.getStationName())
                .distance(entity.getDistance())
                .walkingTime(entity.getWalkingTime())
                .commuteTime(convertToCommuteTimeResponse(entity.getCommuteTime()))
                .build();
    }

    private CommuteTimeResponse convertToCommuteTimeResponse(CommuteTimeEntity entity) {
        if (entity == null) {
            return null;
        }
        return CommuteTimeResponse.builder()
                .selfBefore9am(entity.getSelfBefore9am())
                .selfAfter6pm(entity.getSelfAfter6pm())
                .spouseBefore9am(entity.getSpouseBefore9am())
                .spouseAfter6pm(entity.getSpouseAfter6pm())
                .build();
    }

    /**
     * RegionalCharacteristicEntity → RegionalCharacteristicResponse 변환
     */
    private RegionalCharacteristicResponse convertToRegionalCharacteristicResponse(RegionalCharacteristicEntity entity) {
        if (entity == null) {
            return null;
        }
        return RegionalCharacteristicResponse.builder()
                .regionCode(entity.getRegionCode())
                .regionDescription(entity.getRegionDescription())
                .ltv(entity.getLtv())
                .dti(entity.getDti())
                .build();
    }
}
