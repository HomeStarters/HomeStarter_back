package com.dwj.homestarter.calculator.service;

import com.dwj.homestarter.calculator.domain.CalculationResult;
import com.dwj.homestarter.calculator.domain.CalculatorDomain;
import com.dwj.homestarter.calculator.domain.ExternalDataBundle;
import com.dwj.homestarter.calculator.dto.external.AssetDto;
import com.dwj.homestarter.calculator.dto.external.HousingDto;
import com.dwj.homestarter.calculator.dto.external.LoanProductDto;
import com.dwj.homestarter.calculator.dto.external.RegionalCharacteristicDto;
import com.dwj.homestarter.calculator.dto.external.UserProfileDto;
import com.dwj.homestarter.calculator.dto.external.wrapper.*;
import com.dwj.homestarter.calculator.dto.request.HousingExpensesRequest;
import com.dwj.homestarter.calculator.dto.response.*;
import com.dwj.homestarter.calculator.repository.entity.CalculationResultEntity;
import com.dwj.homestarter.calculator.repository.jpa.CalculatorRepository;
import com.dwj.homestarter.calculator.service.client.AssetServiceClient;
import com.dwj.homestarter.calculator.service.client.HousingServiceClient;
import com.dwj.homestarter.calculator.service.client.LoanServiceClient;
import com.dwj.homestarter.calculator.service.client.UserServiceClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 지출 계산 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseCalculatorServiceImpl implements ExpenseCalculatorService {

    private final CalculatorRepository calculatorRepository;
    private final CacheService cacheService;
    private final UserServiceClient userServiceClient;
    private final AssetServiceClient assetServiceClient;
    private final HousingServiceClient housingServiceClient;
    private final LoanServiceClient loanServiceClient;
    private final CalculatorDomain calculatorDomain;
    private final ObjectMapper objectMapper;

    private static final Duration CALC_CACHE_TTL = Duration.ofHours(1);
    private static final Duration LIST_CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration DETAIL_CACHE_TTL = Duration.ofHours(1);

    /**
     * 입주 후 지출 계산
     */
    @Override
    @Transactional
    public CalculationResultResponse calculateHousingExpenses(HousingExpensesRequest request, String userId) {
        log.info("입주 후 지출 계산 시작 - userId: {}, housingId: {}", userId, request.getHousingId());

        // 1. 캐시 조회
        String cacheKey = generateCacheKey(userId, request.getHousingId(), request.getLoanProductId());
        Optional<Object> cachedResult = cacheService.get(cacheKey);
//        if (cachedResult.isPresent()) {
//            log.info("캐시에서 계산 결과 반환 - cacheKey: {}", cacheKey);
//            return (CalculationResultResponse) cachedResult.get();
//        }

        // 2. 외부 데이터 수집
        ExternalDataBundle dataBundle = fetchExternalData(userId, request.getHousingId(), request.getLoanProductId());
        log.info(dataBundle.toString());

        // 3. Domain 계산 수행
        CalculationResult calcResult = calculatorDomain.calculate(
                dataBundle.getUser(),
                dataBundle.getAsset(),
                dataBundle.getHousing(),
                dataBundle.getLoan(),
                request.getLoanAmount(),
                request.getLoanTerm()
        );

        // 4. Entity 생성 및 저장
        CalculationResultEntity entity = buildEntity(userId, request, dataBundle, calcResult);
        CalculationResultEntity savedEntity = calculatorRepository.save(entity);

        // 5. Response 변환
        CalculationResultResponse response = mapToResponse(savedEntity);

        // 6. 캐시 저장
        cacheService.set(cacheKey, response, CALC_CACHE_TTL);

        // 7. 목록 캐시 무효화
        String listCachePattern = "calc:list:" + userId + ":*";
        cacheService.deletePattern(listCachePattern);

        log.info("입주 후 지출 계산 완료 - resultId: {}, status: {}", savedEntity.getId(), savedEntity.getStatus());
        return response;
    }

    /**
     * 계산 결과 목록 조회
     */
    @Override
    public CalculationResultListResponse getCalculationResults(String userId, String housingId,
                                                                 String status, Pageable pageable) {
        log.info("계산 결과 목록 조회 - userId: {}, housingId: {}, status: {}", userId, housingId, status);

        // 1. 목록 캐시 조회
        String listCacheKey = String.format("calc:list:%s:%d:%d", userId, pageable.getPageNumber(), pageable.getPageSize());
        Optional<Object> cachedList = cacheService.get(listCacheKey);
//        if (cachedList.isPresent()) {
//            log.info("캐시에서 목록 반환 - cacheKey: {}", listCacheKey);
//            return (CalculationResultListResponse) cachedList.get();
//        }

        // 2. Repository 조회
        Page<CalculationResultEntity> page;
        if (housingId != null && status != null) {
            // 주택 ID와 상태 모두 필터링 (복합 조건)
            page = calculatorRepository.findByUserIdAndHousingId(userId, housingId, pageable)
                    .map(entity -> entity.getStatus().equals(status) ? entity : null);
        } else if (housingId != null) {
            page = calculatorRepository.findByUserIdAndHousingId(userId, housingId, pageable);
        } else if (status != null) {
            page = calculatorRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            page = calculatorRepository.findByUserId(userId, pageable);
        }

        // 3. Response 변환
        List<CalculationResultListItem> items = page.getContent().stream()
                .map(this::mapToListItem)
                .collect(Collectors.toList());

        CalculationResultListResponse response = CalculationResultListResponse.builder()
                .results(items)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .total(page.getTotalElements())
                .build();

        // 4. 목록 캐시 저장
        cacheService.set(listCacheKey, response, LIST_CACHE_TTL);

        log.info("계산 결과 목록 조회 완료 - 총 {}건", items.size());
        return response;
    }

    /**
     * 계산 결과 상세 조회
     */
    @Override
    public CalculationResultResponse getCalculationResult(String id, String userId) {
        log.info("계산 결과 상세 조회 - id: {}, userId: {}", id, userId);

        // 1. 상세 캐시 조회
        String detailCacheKey = "calc:detail:" + id;
        Optional<Object> cachedDetail = cacheService.get(detailCacheKey);
//        if (cachedDetail.isPresent()) {
//            log.info("캐시에서 상세 정보 반환 - cacheKey: {}", detailCacheKey);
//            return (CalculationResultResponse) cachedDetail.get();
//        }

        // 2. Repository 조회
        CalculationResultEntity entity = calculatorRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("계산 결과를 찾을 수 없습니다: " + id));

        // 3. Response 변환
        CalculationResultResponse response = mapToResponse(entity);

        // 4. 상세 캐시 저장
        cacheService.set(detailCacheKey, response, DETAIL_CACHE_TTL);

        log.info("계산 결과 상세 조회 완료 - id: {}", id);
        return response;
    }

    /**
     * 계산 결과 삭제
     */
    @Override
    @Transactional
    public void deleteCalculationResult(String id, String userId) {
        log.info("계산 결과 삭제 - id: {}, userId: {}", id, userId);

        // 1. 권한 확인
        CalculationResultEntity entity = calculatorRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("계산 결과를 찾을 수 없거나 권한이 없습니다: " + id));

        // 2. Repository 삭제
        calculatorRepository.deleteById(id);

        // 3. 캐시 무효화
        String detailCacheKey = "calc:detail:" + id;
        cacheService.delete(detailCacheKey);

        String listCachePattern = "calc:list:" + userId + ":*";
        cacheService.deletePattern(listCachePattern);

        log.info("계산 결과 삭제 완료 - id: {}", id);
    }

    /**
     * 캐시 키 생성
     */
    private String generateCacheKey(String userId, String housingId, String loanProductId) {
        return String.format("calc:%s:%s:%s", userId, housingId, loanProductId);
    }

    /**
     * 외부 데이터 수집 및 내부 DTO 변환
     */
    private ExternalDataBundle fetchExternalData(String userId, String housingId, String loanProductId) {
        log.debug("외부 데이터 수집 시작");

        // 1. User Service 호출 및 변환
        ApiResponse<UserProfileResponse> userResponse = userServiceClient.getUserProfile();
        UserProfileDto user = convertUserProfile(userResponse.getData());

        // 2. Asset Service 호출 및 변환
        AssetListResponse assetResponse = assetServiceClient.getAssetInfo();
        AssetDto asset = convertAsset(assetResponse);

        // 3. Housing Service 호출 및 변환
        ApiResponse<HousingResponse> housingResponse = housingServiceClient.getHousingInfo(housingId);
        HousingDto housing = convertHousing(housingResponse.getData());

        // 4. Loan Service 호출 및 변환
        LoanProductResponse loanResponse = loanServiceClient.getLoanProduct(loanProductId);
        LoanProductDto loan = convertLoanProduct(loanResponse.getData());

        log.debug("외부 데이터 수집 완료");
        return ExternalDataBundle.builder()
                .user(user)
                .asset(asset)
                .housing(housing)
                .loan(loan)
                .build();
    }

    /**
     * UserProfileResponse → UserProfileDto 변환
     */
    private UserProfileDto convertUserProfile(UserProfileResponse response) {
        return UserProfileDto.builder()
                .userId(response.getUserId())
                .birthDate(response.getBirthDate())
                .gender(response.getGender())
                .residence(response.getCurrentAddress())
                .workLocation(response.getUserWorkplaceAddress())
                .withholdingTaxSalary(response.getWithholdingTaxSalary())
                .build();
    }

    /**
     * AssetListResponse → AssetDto 변환
     * combinedSummary에서 합산 데이터를 추출하고, 개별 대출 항목을 수집
     */
    private AssetDto convertAsset(AssetListResponse response) {
        AssetListResponse.CombinedAssetSummaryDto summary = response.getCombinedSummary();
        String userId = null;
        List<AssetDto.LoanItemInfo> loanItems = new java.util.ArrayList<>();

        if (response.getAssets() != null && !response.getAssets().isEmpty()) {
            userId = response.getAssets().get(0).getUserId();
            for (AssetListResponse.AssetResponse asset : response.getAssets()) {
                if (asset.getLoans() != null) {
                    for (AssetListResponse.LoanItemInfo loan : asset.getLoans()) {
                        loanItems.add(AssetDto.LoanItemInfo.builder()
                                .amount(loan.getAmount())
                                .interestRate(loan.getInterestRate())
                                .expirationDate(loan.getExpirationDate())
                                .isExcludingCalculation(loan.isExcludingCalculation())
                                .build());
                    }
                }
            }
        }

        return AssetDto.builder()
                .userId(userId)
                .totalAssets(summary.getTotalAssets())
                .totalLoans(summary.getTotalLoans())
                .monthlyIncome(summary.getTotalMonthlyIncome())
                .monthlyExpenses(summary.getTotalMonthlyExpense())
                .loanItems(loanItems)
                .build();
    }

    /**
     * HousingResponse → HousingDto 변환
     */
    private HousingDto convertHousing(HousingResponse response) {
        // moveInDate: "YYYY-MM" 문자열 → LocalDate (해당 월의 1일로 변환)
        LocalDate moveInDate = null;
        if (response.getMoveInDate() != null && !response.getMoveInDate().isEmpty()) {
            YearMonth ym = YearMonth.parse(response.getMoveInDate(), DateTimeFormatter.ofPattern("yyyy-MM"));
            moveInDate = ym.atDay(1);
        }

        // regionalCharacteristic 변환
        RegionalCharacteristicDto regionalCharacteristic = null;
        if (response.getRegionalCharacteristic() != null) {
            HousingResponse.RegionalCharacteristicResponse rc = response.getRegionalCharacteristic();
            regionalCharacteristic = RegionalCharacteristicDto.builder()
                    .regionCode(rc.getRegionCode())
                    .regionDescription(rc.getRegionDescription())
                    .ltv(rc.getLtv() != null ? rc.getLtv().doubleValue() : null)
                    .dti(rc.getDti() != null ? rc.getDti().doubleValue() : null)
                    .build();
        }

        return HousingDto.builder()
                .housingId(String.valueOf(response.getId()))
                .name(response.getHousingName())
                .type(response.getHousingType() != null ? response.getHousingType() : null)
                .price(response.getPrice() != null ? response.getPrice().longValue() : 0L)
                .moveInDate(moveInDate)
                .regionalCharacteristic(regionalCharacteristic)
                .build();
    }

    /**
     * LoanProductData → LoanProductDto 변환
     */
    private LoanProductDto convertLoanProduct(LoanProductResponse.LoanProductData data) {
        return LoanProductDto.builder()
                .loanProductId(String.valueOf(data.getId()))
                .name(data.getName())
//                .ltvLimit(data.getLtvLimit())
//                .dtiLimit(data.getDtiLimit())
                .dsrLimit(data.getDsrLimit())
                .isApplyLtv(data.getIsApplyLtv())
                .isApplyDti(data.getIsApplyDti())
                .isApplyDsr(data.getIsApplyDsr())
                .interestRate(data.getInterestRate())
                .maxAmount(data.getLoanLimit())
                .build();
    }

    /**
     * Entity 생성
     */
    private CalculationResultEntity buildEntity(String userId, HousingExpensesRequest request,
                                                  ExternalDataBundle dataBundle,
                                                  CalculationResult calcResult) {
        String ineligibilityReasonsJson = null;
        if (calcResult.getIneligibilityReasons() != null && !calcResult.getIneligibilityReasons().isEmpty()) {
            try {
                ineligibilityReasonsJson = objectMapper.writeValueAsString(calcResult.getIneligibilityReasons());
            } catch (JsonProcessingException e) {
                log.error("미충족 사유 JSON 변환 실패", e);
                ineligibilityReasonsJson = "[]";
            }
        }

        // 지역특성에서 LTV/DTI 한도 가져오기 (비율 → 백분율 변환)
        Double ltvLimit = null;
        Double dtiLimit = null;
        RegionalCharacteristicDto rc = dataBundle.getHousing().getRegionalCharacteristic();
        if (rc != null) {
            if (rc.getLtv() != null) {
                ltvLimit = rc.getLtv() * 100;
            }
            if (rc.getDti() != null) {
                dtiLimit = rc.getDti() * 100;
            }
        }

        return CalculationResultEntity.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .housingId(request.getHousingId())
                .housingName(dataBundle.getHousing().getName())
                .moveInDate(dataBundle.getHousing().getMoveInDate())
                .loanProductId(request.getLoanProductId())
                .loanProductName(dataBundle.getLoan().getName())
                .loanAmount(request.getLoanAmount())
                .loanTerm(request.getLoanTerm())
                .currentAssets(calcResult.getCurrentAssets())
                .estimatedAssets(calcResult.getEstimatedAssets())
                .loanRequired(calcResult.getLoanRequired())
                .ltv(calcResult.getLtv())
                .dti(calcResult.getDti())
                .dsr(calcResult.getDsr())
                .ltvLimit(ltvLimit)
                .dtiLimit(dtiLimit)
                .dsrLimit(dataBundle.getLoan().getDsrLimit())
                .isEligible(calcResult.getIsEligible())
                .ineligibilityReasons(ineligibilityReasonsJson)
                .monthlyPayment(calcResult.getMonthlyPayment())
                .afterMoveInAssets(calcResult.getAfterMoveInAssets())
                .afterMoveInMonthlyExpenses(calcResult.getAfterMoveInMonthlyExpenses())
                .afterMoveInMonthlyIncome(calcResult.getAfterMoveInMonthlyIncome())
                .afterMoveInAvailableFunds(calcResult.getAfterMoveInAvailableFunds())
                .status(calcResult.getIsEligible() ? "ELIGIBLE" : "INELIGIBLE")
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Entity를 Response로 변환
     */
    private CalculationResultResponse mapToResponse(CalculationResultEntity entity) {
        List<String> ineligibilityReasons = null;
        if (entity.getIneligibilityReasons() != null) {
            try {
                ineligibilityReasons = objectMapper.readValue(entity.getIneligibilityReasons(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (JsonProcessingException e) {
                log.error("미충족 사유 JSON 파싱 실패", e);
                ineligibilityReasons = List.of();
            }
        }

        return CalculationResultResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .housingId(entity.getHousingId())
                .housingName(entity.getHousingName())
                .moveInDate(entity.getMoveInDate())
                .loanProductId(entity.getLoanProductId())
                .loanProductName(entity.getLoanProductName())
                .loanAmount(entity.getLoanAmount())
                .calculatedAt(entity.getCalculatedAt())
                .financialStatus(FinancialStatusDto.builder()
                        .currentAssets(entity.getCurrentAssets())
                        .estimatedAssets(entity.getEstimatedAssets())
                        .loanRequired(entity.getLoanRequired())
                        .build())
                .loanAnalysis(LoanAnalysisDto.builder()
                        .ltv(entity.getLtv())
                        .dti(entity.getDti())
                        .dsr(entity.getDsr())
                        .ltvLimit(entity.getLtvLimit())
                        .dtiLimit(entity.getDtiLimit())
                        .dsrLimit(entity.getDsrLimit())
                        .isEligible(entity.getIsEligible())
                        .ineligibilityReasons(ineligibilityReasons)
                        .monthlyPayment(entity.getMonthlyPayment())
                        .build())
                .afterMoveIn(AfterMoveInDto.builder()
                        .assets(entity.getAfterMoveInAssets())
                        .monthlyExpenses(entity.getAfterMoveInMonthlyExpenses())
                        .monthlyIncome(entity.getAfterMoveInMonthlyIncome())
                        .monthlyAvailableFunds(entity.getAfterMoveInAvailableFunds())
                        .build())
                .status(entity.getStatus())
                .build();
    }

    /**
     * Entity를 ListItem으로 변환
     */
    private CalculationResultListItem mapToListItem(CalculationResultEntity entity) {
        return CalculationResultListItem.builder()
                .id(entity.getId())
                .housingName(entity.getHousingName())
                .loanProductName(entity.getLoanProductName())
                .calculatedAt(entity.getCalculatedAt())
                .status(entity.getStatus())
                .monthlyAvailableFunds(entity.getAfterMoveInAvailableFunds())
                .build();
    }
}
