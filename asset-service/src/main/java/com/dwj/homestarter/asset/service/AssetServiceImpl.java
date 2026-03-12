package com.dwj.homestarter.asset.service;

import com.dwj.homestarter.asset.domain.*;
import com.dwj.homestarter.asset.dto.*;
import com.dwj.homestarter.asset.dto.request.CreateAssetRequest;
import com.dwj.homestarter.asset.dto.request.UpdateAssetRequest;
import com.dwj.homestarter.asset.dto.response.AssetListResponse;
import com.dwj.homestarter.asset.dto.response.AssetResponse;
import com.dwj.homestarter.asset.dto.response.CombinedAssetSummaryDto;
import com.dwj.homestarter.asset.dto.response.HouseholdAssetResponse;
import com.dwj.homestarter.asset.dto.response.HouseholdMemberAssetResponse;
import com.dwj.homestarter.asset.client.CalculatorServiceClient;
import com.dwj.homestarter.asset.client.UserServiceClient;
import com.dwj.homestarter.asset.client.dto.HouseholdMemberInfo;
import com.dwj.homestarter.asset.client.dto.HouseholdMembersData;
import com.dwj.homestarter.asset.event.AssetEventPublisher;
import com.dwj.homestarter.asset.repository.entity.*;
import com.dwj.homestarter.asset.repository.jpa.*;
import com.dwj.homestarter.common.exception.BusinessException;
import com.dwj.homestarter.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 자산정보 관리 서비스 구현
 * 자산정보의 CRUD 및 총액 계산 로직 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetItemRepository assetItemRepository;
    private final LoanItemRepository loanItemRepository;
    private final IncomeItemRepository incomeItemRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final ValidationService validationService;
    private final AssetEventPublisher assetEventPublisher;
    private final UserServiceClient userServiceClient;
    private final CalculatorServiceClient calculatorServiceClient;

    @Override
    @Transactional
    public AssetResponse createSelfAssets(String userId, CreateAssetRequest request) {
        log.info("본인 자산정보 생성 시작 - userId: {}", userId);
        return createAssets(userId, OwnerType.SELF, request);
    }

    @Override
    @Transactional
    public AssetResponse createSpouseAssets(String userId, CreateAssetRequest request) {
        log.info("배우자 자산정보 생성 시작 - userId: {}", userId);
        return createAssets(userId, OwnerType.SPOUSE, request);
    }

    /**
     * 자산정보 생성 공통 로직
     */
    private AssetResponse createAssets(String userId, OwnerType ownerType, CreateAssetRequest request) {
        // 중복 체크
        checkDuplicateAsset(userId, ownerType);

        // 검증
        validationService.validateAssetRequest(request);

        // 총액 계산
        long totalAssets = calculateSum(request.getAssets(), AssetItemDto::getAmount);
        long totalLoans = calculateSum(request.getLoans(), LoanItemDto::getAmount);
        long totalMonthlyIncome = calculateSum(request.getMonthlyIncomes(), IncomeItemDto::getAmount);
        long totalMonthlyExpense = calculateSum(request.getMonthlyExpenses(), ExpenseItemDto::getAmount);

        // Asset 도메인 객체 생성
        String assetId = UUID.randomUUID().toString();
        Asset asset = Asset.builder()
                .id(assetId)
                .userId(userId)
                .ownerType(ownerType)
                .build();
        asset.updateTotals(totalAssets, totalLoans, totalMonthlyIncome, totalMonthlyExpense);

        // 엔티티 저장
        AssetEntity assetEntity = AssetEntity.fromDomain(asset);
        assetRepository.save(assetEntity);

        // 항목 저장
        saveAssetItems(assetId, request);

        // 이벤트 발행
        AssetSummary summary = AssetSummary.fromAsset(asset);
        assetEventPublisher.publishAssetUpdated(userId, ownerType, "CREATED", summary);

        log.info("자산정보 생성 완료 - assetId: {}, ownerType: {}", assetId, ownerType);

        return buildAssetResponse(asset, request);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetListResponse getAssets(String userId, OwnerType ownerType) {
        log.info("자산정보 조회 시작 - userId: {}, ownerType: {}", userId, ownerType);

        List<AssetEntity> assetEntities;
        if (ownerType != null) {
            assetEntities = assetRepository.findByUserIdAndOwnerType(userId, ownerType.name())
                    .map(List::of)
                    .orElse(new ArrayList<>());
        } else {
            assetEntities = assetRepository.findByUserId(userId);
        }

        List<AssetResponse> assetResponses = assetEntities.stream()
                .map(this::buildAssetResponseFromEntity)
                .collect(Collectors.toList());

        // 합산 정보 계산
        CombinedAssetSummaryDto combinedSummary = calculateCombinedSummary(assetEntities);

        return AssetListResponse.builder()
                .assets(assetResponses)
                .combinedSummary(combinedSummary)
                .build();
    }

    @Override
    @Transactional
    public AssetResponse updateAsset(String id, String userId, UpdateAssetRequest request) {
        log.info("자산정보 수정 시작 - assetId: {}, userId: {}", id, userId);

        // 자산정보 조회
        AssetEntity assetEntity = assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다: " + id));

        // 권한 확인
        if (!assetEntity.getUserId().equals(userId)) {
            throw new BusinessException("ASSET_003", "자산정보 수정 권한이 없습니다");
        }

        // 검증
        validationService.validateUpdateRequest(request);

        // 기존 항목 삭제
        assetRepository.deleteById(id);
        deleteAssetItems(id);

        // 총액 재계산
        long totalAssets = calculateSum(request.getAssets(), AssetItemDto::getAmount);
        long totalLoans = calculateSum(request.getLoans(), LoanItemDto::getAmount);
        long totalMonthlyIncome = calculateSum(request.getMonthlyIncomes(), IncomeItemDto::getAmount);
        long totalMonthlyExpense = calculateSum(request.getMonthlyExpenses(), ExpenseItemDto::getAmount);

        // Asset 엔티티 업데이트
        Asset asset = assetEntity.toDomain();
        asset.updateTotals(totalAssets, totalLoans, totalMonthlyIncome, totalMonthlyExpense);
        AssetEntity updatedEntity = AssetEntity.fromDomain(asset);
        updatedEntity.setId(id);
        assetRepository.save(updatedEntity);

        // 새 항목 저장
        CreateAssetRequest createRequest = CreateAssetRequest.builder()
                .assets(request.getAssets())
                .loans(request.getLoans())
                .monthlyIncomes(request.getMonthlyIncomes())
                .monthlyExpenses(request.getMonthlyExpenses())
                .build();
        saveAssetItems(id, createRequest);

        // 이벤트 발행
        AssetSummary summary = AssetSummary.fromAsset(asset);
        assetEventPublisher.publishAssetUpdated(userId, asset.getOwnerType(), "UPDATED", summary);

        log.info("자산정보 수정 완료 - assetId: {}", id);

        return buildAssetResponseFromEntity(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteAsset(String id, String userId) {
        log.info("자산정보 삭제 시작 - assetId: {}, userId: {}", id, userId);

        // 자산정보 조회
        AssetEntity assetEntity = assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다: " + id));

        // 권한 확인
        if (!assetEntity.getUserId().equals(userId)) {
            throw new BusinessException("ASSET_004", "자산정보 삭제 권한이 없습니다");
        }

        // 항목 삭제 (CASCADE로 자동 삭제되지만 명시적으로 처리)
        deleteAssetItems(id);

        // 자산정보 삭제
        assetRepository.deleteById(id);

        // 이벤트 발행
        AssetSummary summary = AssetSummary.builder()
                .totalAssets(0L)
                .totalLoans(0L)
                .totalMonthlyIncome(0L)
                .totalMonthlyExpense(0L)
                .netAssets(0L)
                .monthlyAvailableFunds(0L)
                .build();
        assetEventPublisher.publishAssetUpdated(userId, OwnerType.valueOf(assetEntity.getOwnerType()), "DELETED", summary);

        log.info("자산정보 삭제 완료 - assetId: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetSummary calculateTotals(String userId, OwnerType ownerType) {
        AssetEntity assetEntity = assetRepository.findByUserIdAndOwnerType(userId, ownerType.name())
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다"));

        return AssetSummary.fromAsset(assetEntity.toDomain());
    }

    @Override
    @Transactional
    public AssetResponse createAssetByUserId(String userId, OwnerType ownerType, CreateAssetRequest request) {
        log.info("사용자 ID로 자산정보 직접 생성 시작 - userId: {}, ownerType: {}", userId, ownerType);

        // 검증
        validationService.validateAssetRequest(request);

        // 총액 계산
        long totalAssets = calculateSum(request.getAssets(), AssetItemDto::getAmount);
        long totalLoans = calculateSum(request.getLoans(), LoanItemDto::getAmount);
        long totalMonthlyIncome = calculateSum(request.getMonthlyIncomes(), IncomeItemDto::getAmount);
        long totalMonthlyExpense = calculateSum(request.getMonthlyExpenses(), ExpenseItemDto::getAmount);

        // Asset 도메인 객체 생성 (ID 자동 채번)
        String assetId = UUID.randomUUID().toString();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Asset asset = Asset.builder()
                .id(assetId)
                .userId(userId)
                .ownerType(ownerType)
                .createdAt(now)
                .updatedAt(now)
                .build();
        asset.updateTotals(totalAssets, totalLoans, totalMonthlyIncome, totalMonthlyExpense);

        // 엔티티 저장
        AssetEntity assetEntity = AssetEntity.fromDomain(asset);
        assetRepository.save(assetEntity);

        // 항목 저장
        saveAssetItems(assetId, request);

        // 이벤트 발행
        AssetSummary summary = AssetSummary.fromAsset(asset);
        assetEventPublisher.publishAssetUpdated(userId, ownerType, "CREATED", summary);

        log.info("사용자 ID로 자산정보 직접 생성 완료 - assetId: {}, userId: {}, ownerType: {}", assetId, userId, ownerType);

        return buildAssetResponse(asset, request);
    }

    @Override
    @Transactional(readOnly = true)
    public HouseholdAssetResponse getHouseholdAssets(String userId, String token) {
        log.info("가구원 전체 자산정보 조회 시작 - userId: {}", userId);

        // 1. user-service에서 가구원 목록 조회
        Optional<HouseholdMembersData> householdData = userServiceClient.getHouseholdMembers(token);

        // 2. 가구에 미가입인 경우 본인 자산만 반환
        if (householdData.isEmpty() || householdData.get().getMembers() == null
                || householdData.get().getMembers().isEmpty()) {
            log.info("가구 미가입 사용자 - 본인 자산만 반환. userId: {}", userId);
            return buildSoloHouseholdResponse(userId);
        }

        HouseholdMembersData data = householdData.get();
        List<HouseholdMemberInfo> memberInfos = data.getMembers();
        List<String> memberUserIds = memberInfos.stream()
                .map(HouseholdMemberInfo::getUserId)
                .collect(Collectors.toList());

        // 3. 가구원 전체 userId로 자산 일괄 조회
        List<AssetEntity> allAssets = assetRepository.findByUserIdIn(memberUserIds);

        // 4. userId 기준으로 그룹핑
        Map<String, List<AssetEntity>> assetsByUser = allAssets.stream()
                .collect(Collectors.groupingBy(AssetEntity::getUserId));

        // 5. 가구원별 응답 조립
        List<HouseholdMemberAssetResponse> memberResponses = memberInfos.stream()
                .map(member -> {
                    List<AssetEntity> memberAssets = assetsByUser.getOrDefault(member.getUserId(), List.of());
                    List<AssetResponse> assetResponses = memberAssets.stream()
                            .map(this::buildAssetResponseFromEntity)
                            .collect(Collectors.toList());
                    CombinedAssetSummaryDto memberSummary = calculateCombinedSummary(memberAssets);

                    return HouseholdMemberAssetResponse.builder()
                            .userId(member.getUserId())
                            .userName(member.getName())
                            .role(member.getRole())
                            .assets(AssetListResponse.builder()
                                    .assets(assetResponses)
                                    .combinedSummary(memberSummary)
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());

        // 6. 가구 전체 합산
        CombinedAssetSummaryDto householdSummary = calculateCombinedSummary(allAssets);

        log.info("가구원 전체 자산정보 조회 완료 - householdId: {}, 가구원 수: {}",
                data.getHouseholdId(), memberInfos.size());

        return HouseholdAssetResponse.builder()
                .householdId(data.getHouseholdId())
                .members(memberResponses)
                .householdSummary(householdSummary)
                .build();
    }

    /**
     * 가구 미가입 사용자의 단독 응답 생성
     */
    private HouseholdAssetResponse buildSoloHouseholdResponse(String userId) {
        List<AssetEntity> userAssets = assetRepository.findByUserId(userId);
        List<AssetResponse> assetResponses = userAssets.stream()
                .map(this::buildAssetResponseFromEntity)
                .collect(Collectors.toList());
        CombinedAssetSummaryDto summary = calculateCombinedSummary(userAssets);

        HouseholdMemberAssetResponse selfMember = HouseholdMemberAssetResponse.builder()
                .userId(userId)
                .userName(null)
                .role("OWNER")
                .assets(AssetListResponse.builder()
                        .assets(assetResponses)
                        .combinedSummary(summary)
                        .build())
                .build();

        return HouseholdAssetResponse.builder()
                .householdId(null)
                .members(List.of(selfMember))
                .householdSummary(summary)
                .build();
    }

    /**
     * 중복 자산정보 체크
     */
    private void checkDuplicateAsset(String userId, OwnerType ownerType) {
        if (assetRepository.existsByUserIdAndOwnerType(userId, ownerType.name())) {
            throw new BusinessException("ASSET_001", "이미 " + (ownerType == OwnerType.SELF ? "본인" : "배우자") + " 자산정보가 존재합니다");
        }
    }

    /**
     * 자산 항목 저장
     */
    private void saveAssetItems(String assetId, CreateAssetRequest request) {
        // 자산 항목
        List<AssetItemEntity> assetItems = request.getAssets().stream()
                .map(dto -> AssetItemEntity.fromDomain(assetId, dto.toDomain()))
                .collect(Collectors.toList());
        if (!assetItems.isEmpty()) {
            assetItemRepository.saveAll(assetItems);
        }

        // 대출 항목
        List<LoanItemEntity> loanItems = request.getLoans().stream()
                .map(dto -> LoanItemEntity.fromDomain(assetId, dto.toDomain()))
                .collect(Collectors.toList());
        if (!loanItems.isEmpty()) {
            loanItemRepository.saveAll(loanItems);
        }

        // 월소득 항목
        List<IncomeItemEntity> incomeItems = request.getMonthlyIncomes().stream()
                .map(dto -> IncomeItemEntity.fromDomain(assetId, dto.toDomain()))
                .collect(Collectors.toList());
        if (!incomeItems.isEmpty()) {
            incomeItemRepository.saveAll(incomeItems);
        }

        // 월지출 항목
        List<ExpenseItemEntity> expenseItems = request.getMonthlyExpenses().stream()
                .map(dto -> ExpenseItemEntity.fromDomain(assetId, dto.toDomain()))
                .collect(Collectors.toList());
        if (!expenseItems.isEmpty()) {
            expenseItemRepository.saveAll(expenseItems);
        }
    }

    /**
     * 자산 항목 삭제
     */
    private void deleteAssetItems(String assetId) {
        assetItemRepository.deleteByAssetId(assetId);
        loanItemRepository.deleteByAssetId(assetId);
        incomeItemRepository.deleteByAssetId(assetId);
        expenseItemRepository.deleteByAssetId(assetId);
    }

    /**
     * AssetResponse 생성 (엔티티로부터)
     */
    private AssetResponse buildAssetResponseFromEntity(AssetEntity assetEntity) {
        String assetId = assetEntity.getId();

        List<AssetItemDto> assetItems = assetItemRepository.findByAssetId(assetId).stream()
                .map(e -> AssetItemDto.fromDomain(e.toDomain()))
                .collect(Collectors.toList());

        List<LoanItemDto> loanItems = loanItemRepository.findByAssetId(assetId).stream()
                .map(e -> LoanItemDto.fromDomain(e.toDomain()))
                .collect(Collectors.toList());

        List<IncomeItemDto> incomeItems = incomeItemRepository.findByAssetId(assetId).stream()
                .map(e -> IncomeItemDto.fromDomain(e.toDomain()))
                .collect(Collectors.toList());

        List<ExpenseItemDto> expenseItems = expenseItemRepository.findByAssetId(assetId).stream()
                .map(e -> ExpenseItemDto.fromDomain(e.toDomain()))
                .collect(Collectors.toList());

        return AssetResponse.from(assetEntity.toDomain(), assetItems, loanItems, incomeItems, expenseItems, "success");
    }

    /**
     * AssetResponse 생성 (요청으로부터)
     */
    private AssetResponse buildAssetResponse(Asset asset, CreateAssetRequest request) {
        return AssetResponse.from(asset, request.getAssets(), request.getLoans(),
                request.getMonthlyIncomes(), request.getMonthlyExpenses(), "success");
    }

    /**
     * 합산 정보 계산
     */
    private CombinedAssetSummaryDto calculateCombinedSummary(List<AssetEntity> assetEntities) {
        long totalAssets = assetEntities.stream().mapToLong(AssetEntity::getTotalAssets).sum();
        long totalLoans = assetEntities.stream().mapToLong(AssetEntity::getTotalLoans).sum();
        long totalMonthlyIncome = assetEntities.stream().mapToLong(AssetEntity::getTotalMonthlyIncome).sum();
        long totalMonthlyExpense = assetEntities.stream().mapToLong(AssetEntity::getTotalMonthlyExpense).sum();

        return CombinedAssetSummaryDto.builder()
                .totalAssets(totalAssets)
                .totalLoans(totalLoans)
                .totalMonthlyIncome(totalMonthlyIncome)
                .totalMonthlyExpense(totalMonthlyExpense)
                .netAssets(totalAssets - totalLoans)
                .monthlyAvailableFunds(totalMonthlyIncome - totalMonthlyExpense)
                .build();
    }

    /**
     * 리스트 합계 계산 헬퍼 메소드
     */
    private <T> long calculateSum(List<T> items, java.util.function.ToLongFunction<T> mapper) {
        return items.stream().mapToLong(mapper).sum();
    }

    @Override
    @Transactional
    public void deleteAssetItem(String assetType, String itemId, String userId) {
        log.info("자산상세정보 삭제 시작 - assetType: {}, itemId: {}, userId: {}", assetType, itemId, userId);

        // 자산유형에 따라 삭제 처리
        switch (assetType) {
            case "assets" -> deleteAssetItemAndUpdateTotal(itemId, userId);
            case "loans" -> deleteLoanItemAndUpdateTotal(itemId, userId);
            case "monthlyIncome" -> deleteIncomeItemAndUpdateTotal(itemId, userId);
            case "monthlyExpense" -> deleteExpenseItemAndUpdateTotal(itemId, userId);
            default -> throw new BusinessException("ASSET_005", "지원하지 않는 자산유형입니다: " + assetType);
        }

        log.info("자산상세정보 삭제 완료 - assetType: {}, itemId: {}", assetType, itemId);
    }

    /**
     * 자산 항목 삭제 및 총액 업데이트
     */
    private void deleteAssetItemAndUpdateTotal(String itemId, String userId) {
        AssetItemEntity itemEntity = assetItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("자산 항목을 찾을 수 없습니다: " + itemId));

        AssetEntity assetEntity = assetRepository.findById(itemEntity.getAssetId())
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다: " + itemEntity.getAssetId()));

        validateOwnership(assetEntity, userId);

        Long amount = itemEntity.getAmount();
        assetItemRepository.deleteById(itemId);

        // total_assets 차감 및 순자산 재계산
        assetEntity.setTotalAssets(assetEntity.getTotalAssets() - amount);
        assetEntity.setNetAssets(assetEntity.getTotalAssets() - assetEntity.getTotalLoans());
        assetRepository.save(assetEntity);

        // 이벤트 발행
        publishAssetItemDeletedEvent(assetEntity, userId);
    }

    /**
     * 대출 항목 삭제 및 총액 업데이트
     * 연결된 월지출 항목이 있으면 함께 삭제
     */
    private void deleteLoanItemAndUpdateTotal(String itemId, String userId) {
        LoanItemEntity itemEntity = loanItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("대출 항목을 찾을 수 없습니다: " + itemId));

        AssetEntity assetEntity = assetRepository.findById(itemEntity.getAssetId())
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다: " + itemEntity.getAssetId()));

        validateOwnership(assetEntity, userId);

        // 연결된 월지출 항목이 있으면 함께 삭제 (total_monthly_expense 차감)
        expenseItemRepository.findByLoanItemId(itemId).ifPresent(linkedExpense -> {
            log.info("대출 항목 삭제로 인한 연결 월지출 항목 삭제 - loanItemId: {}, expenseItemId: {}",
                    itemId, linkedExpense.getId());
            expenseItemRepository.deleteById(linkedExpense.getId());
            assetEntity.setTotalMonthlyExpense(assetEntity.getTotalMonthlyExpense() - linkedExpense.getAmount());
            assetEntity.setMonthlyAvailableFunds(assetEntity.getTotalMonthlyIncome() - assetEntity.getTotalMonthlyExpense());
        });

        Long amount = itemEntity.getAmount();
        loanItemRepository.deleteById(itemId);

        // total_loans 차감 및 순자산 재계산
        assetEntity.setTotalLoans(assetEntity.getTotalLoans() - amount);
        assetEntity.setNetAssets(assetEntity.getTotalAssets() - assetEntity.getTotalLoans());
        assetRepository.save(assetEntity);

        // 이벤트 발행
        publishAssetItemDeletedEvent(assetEntity, userId);
    }

    /**
     * 월소득 항목 삭제 및 총액 업데이트
     */
    private void deleteIncomeItemAndUpdateTotal(String itemId, String userId) {
        IncomeItemEntity itemEntity = incomeItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("월소득 항목을 찾을 수 없습니다: " + itemId));

        AssetEntity assetEntity = assetRepository.findById(itemEntity.getAssetId())
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다: " + itemEntity.getAssetId()));

        validateOwnership(assetEntity, userId);

        Long amount = itemEntity.getAmount();
        incomeItemRepository.deleteById(itemId);

        // total_monthly_income 차감 및 월 가용자금 재계산
        assetEntity.setTotalMonthlyIncome(assetEntity.getTotalMonthlyIncome() - amount);
        assetEntity.setMonthlyAvailableFunds(assetEntity.getTotalMonthlyIncome() - assetEntity.getTotalMonthlyExpense());
        assetRepository.save(assetEntity);

        // 이벤트 발행
        publishAssetItemDeletedEvent(assetEntity, userId);
    }

    /**
     * 월지출 항목 삭제 및 총액 업데이트
     */
    private void deleteExpenseItemAndUpdateTotal(String itemId, String userId) {
        ExpenseItemEntity itemEntity = expenseItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("월지출 항목을 찾을 수 없습니다: " + itemId));

        AssetEntity assetEntity = assetRepository.findById(itemEntity.getAssetId())
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다: " + itemEntity.getAssetId()));

        validateOwnership(assetEntity, userId);

        Long amount = itemEntity.getAmount();
        expenseItemRepository.deleteById(itemId);

        // total_monthly_expense 차감 및 월 가용자금 재계산
        assetEntity.setTotalMonthlyExpense(assetEntity.getTotalMonthlyExpense() - amount);
        assetEntity.setMonthlyAvailableFunds(assetEntity.getTotalMonthlyIncome() - assetEntity.getTotalMonthlyExpense());
        assetRepository.save(assetEntity);

        // 이벤트 발행
        publishAssetItemDeletedEvent(assetEntity, userId);
    }

    /**
     * 자산정보 소유권 검증
     */
    private void validateOwnership(AssetEntity assetEntity, String userId) {
        if (!assetEntity.getUserId().equals(userId)) {
            throw new BusinessException("ASSET_006", "자산상세정보 삭제 권한이 없습니다");
        }
    }

    /**
     * 자산상세정보 삭제 이벤트 발행
     */
    private void publishAssetItemDeletedEvent(AssetEntity assetEntity, String userId) {
        Asset asset = assetEntity.toDomain();
        AssetSummary summary = AssetSummary.fromAsset(asset);
        assetEventPublisher.publishAssetUpdated(userId, asset.getOwnerType(), "ITEM_DELETED", summary);
    }

    @Override
    @Transactional
    public ExpenseItemDto registerLoanExpense(String loanItemId, String userId, String token) {
        log.info("대출 기반 월지출 자동 등록 시작 - loanItemId: {}, userId: {}", loanItemId, userId);

        // 대출 항목 조회
        LoanItemEntity loanItem = loanItemRepository.findById(loanItemId)
                .orElseThrow(() -> new NotFoundException("대출 항목을 찾을 수 없습니다: " + loanItemId));

        // 자산정보 조회 및 소유권 검증
        AssetEntity assetEntity = assetRepository.findById(loanItem.getAssetId())
                .orElseThrow(() -> new NotFoundException("자산정보를 찾을 수 없습니다: " + loanItem.getAssetId()));
        validateOwnership(assetEntity, userId);

        // 이미 연결된 월지출 항목 존재 여부 확인
        if (expenseItemRepository.findByLoanItemId(loanItemId).isPresent()) {
            throw new BusinessException("ASSET_007", "해당 대출에 이미 월지출이 등록되어 있습니다");
        }

        // 대출 원금 결정 (실행액 우선, 없으면 잔액)
        Long principalAmount = (loanItem.getExecutedAmount() != null && loanItem.getExecutedAmount() > 0)
                ? loanItem.getExecutedAmount() : loanItem.getAmount();

        // calculator-service 호출하여 월 상환액 계산
        Long monthlyPayment = calculatorServiceClient.calculateMonthlyPayment(
                token,
                loanItem.getLoanType() != null ? loanItem.getLoanType().name() : null,
                loanItem.getRepaymentType() != null ? loanItem.getRepaymentType().name() : null,
                principalAmount,
                loanItem.getInterestRate(),
                loanItem.getRepaymentPeriod(),
                loanItem.getGracePeriod());

        if (monthlyPayment == null || monthlyPayment <= 0) {
            throw new BusinessException("ASSET_008", "월 상환액 계산에 실패했습니다. 대출 정보를 확인해 주세요.");
        }

        // 월지출 항목 생성
        String expenseItemId = UUID.randomUUID().toString();
        ExpenseItemEntity expenseItemEntity = ExpenseItemEntity.builder()
                .id(expenseItemId)
                .assetId(loanItem.getAssetId())
                .name(loanItem.getName() + " 상환")
                .amount(monthlyPayment)
                .loanItemId(loanItemId)
                .build();
        expenseItemRepository.save(expenseItemEntity);

        // 자산 총액 업데이트 (total_monthly_expense 증가)
        assetEntity.setTotalMonthlyExpense(assetEntity.getTotalMonthlyExpense() + monthlyPayment);
        assetEntity.setMonthlyAvailableFunds(assetEntity.getTotalMonthlyIncome() - assetEntity.getTotalMonthlyExpense());
        assetRepository.save(assetEntity);

        // 이벤트 발행
        Asset asset = assetEntity.toDomain();
        AssetSummary summary = AssetSummary.fromAsset(asset);
        assetEventPublisher.publishAssetUpdated(userId, asset.getOwnerType(), "ITEM_CREATED", summary);

        log.info("대출 기반 월지출 자동 등록 완료 - expenseItemId: {}, name: {}, amount: {}",
                expenseItemId, loanItem.getName() + " 상환", monthlyPayment);

        return ExpenseItemDto.fromDomain(expenseItemEntity.toDomain());
    }
}
