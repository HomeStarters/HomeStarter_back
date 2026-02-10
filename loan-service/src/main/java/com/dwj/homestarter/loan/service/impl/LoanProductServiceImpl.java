package com.dwj.homestarter.loan.service.impl;

import com.dwj.homestarter.common.exception.NotFoundException;
import com.dwj.homestarter.common.exception.ValidationException;
import com.dwj.homestarter.loan.domain.LoanProduct;
import com.dwj.homestarter.loan.dto.*;
import com.dwj.homestarter.loan.repository.LoanProductRepository;
import com.dwj.homestarter.loan.service.LoanProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 대출상품 서비스 구현체
 *
 * 대출상품 비즈니스 로직 구현
 *
 * @author homestarter
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanProductServiceImpl implements LoanProductService {

    private static final String LOAN_LIST_CACHE_PREFIX = "loans:list:";
    private static final String LOAN_DETAIL_CACHE_PREFIX = "loan:product:";
    private static final Duration LIST_CACHE_TTL = Duration.ofHours(1);
    private static final Duration DETAIL_CACHE_TTL = Duration.ofHours(2);

    private final LoanProductRepository loanProductRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public LoanProductListResponse getLoanProducts(String housingType, String sortBy, String sortOrder,
                                                   String keyword, Pageable pageable) {
//        // 캐시 키 생성
//        String cacheKey = generateListCacheKey(housingType, sortBy, sortOrder, keyword, pageable);
//
//        // 캐시 조회
//        String cachedData = redisTemplate.opsForValue().get(cacheKey);
//        if (StringUtils.hasText(cachedData)) {
//            try {
//                LoanProductListResponse response = objectMapper.readValue(cachedData, LoanProductListResponse.class);
//                log.debug("Cache hit for loan list: {}", cacheKey);
//                return response;
//            } catch (JsonProcessingException e) {
//                log.warn("Failed to deserialize cached data: {}", e.getMessage());
//            }
//        }

        // DB 조회
        Page<LoanProduct> productPage;
        if (StringUtils.hasText(keyword)) {
            productPage = loanProductRepository.findByNameContainingOrTargetHousingContaining(
                    keyword, keyword, pageable);
//            productPage = loanProductRepository.findByActiveTrueAndNameContainingOrTargetHousingContaining(
//                    keyword, keyword, pageable);
        } else if (StringUtils.hasText(housingType)) {
            productPage = loanProductRepository.findByTargetHousingContaining(housingType, pageable);
//            productPage = loanProductRepository.findByActiveTrueAndTargetHousingContaining(housingType, pageable);
        } else {
            productPage = loanProductRepository.findAll(pageable);
//            productPage = loanProductRepository.findByActiveTrue(pageable);
        }

        // DTO 변환
        List<LoanProductDTO> content = productPage.getContent().stream()
                .map(LoanProductDTO::from)
                .collect(Collectors.toList());

        LoanProductListData data = LoanProductListData.builder()
                .content(content)
                .pageable(PageInfo.from(productPage))
                .build();

        LoanProductListResponse response = LoanProductListResponse.success(data, "대출상품 목록 조회 성공");

//        // 캐시 저장
//        try {
//            String jsonData = objectMapper.writeValueAsString(response);
//            redisTemplate.opsForValue().set(cacheKey, jsonData, LIST_CACHE_TTL);
//            log.debug("Cache stored for loan list: {}", cacheKey);
//        } catch (JsonProcessingException e) {
//            log.warn("Failed to serialize response for caching: {}", e.getMessage());
//        }

        return response;
    }

    @Override
    public LoanProductResponse getLoanProductById(Long id) {
        // 캐시 키 생성
        String cacheKey = LOAN_DETAIL_CACHE_PREFIX + id;

        // 캐시 조회
//        String cachedData = redisTemplate.opsForValue().get(cacheKey);
//        if (StringUtils.hasText(cachedData)) {
//            try {
//                LoanProductResponse response = objectMapper.readValue(cachedData, LoanProductResponse.class);
//                log.debug("Cache hit for loan product: {}", id);
//                return response;
//            } catch (JsonProcessingException e) {
//                log.warn("Failed to deserialize cached data: {}", e.getMessage());
//            }
//        }

        // DB 조회
        LoanProduct product = loanProductRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("대출상품을 찾을 수 없습니다"));

        LoanProductResponse response = LoanProductResponse.success(
                LoanProductDTO.from(product), "대출상품 조회 성공");

        // 캐시 저장
        try {
            String jsonData = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, jsonData, DETAIL_CACHE_TTL);
            log.debug("Cache stored for loan product: {}", id);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize response for caching: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional
    public LoanProductResponse createLoanProduct(CreateLoanProductRequest request) {
        // 데이터 검증
        validateLoanProductData(request.getName(), request.getLoanLimit(), request.getDsrLimit(), request.getInterestRate());

        // Entity 생성 및 저장
        LoanProduct product = request.toEntity();
        LoanProduct savedProduct = loanProductRepository.save(product);

        log.info("Loan product created: {}", savedProduct.getId());

        // 목록 캐시 무효화
        invalidateListCache();

        return LoanProductResponse.success(LoanProductDTO.from(savedProduct), "대출상품 등록 성공");
    }

    @Override
    @Transactional
    public LoanProductResponse updateLoanProduct(Long id, UpdateLoanProductRequest request) {
        // 대출상품 조회
        LoanProduct product = loanProductRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("대출상품을 찾을 수 없습니다"));

        // 데이터 검증
        validateLoanProductData(request.getName(), request.getLoanLimit(), request.getDsrLimit(), request.getInterestRate());

        // 업데이트
        product.update(request.getName(), request.getLoanLimit(), request.getDsrLimit(),
                request.getIsApplyLtv(), request.getIsApplyDti(), request.getIsApplyDsr(),
                request.getInterestRate(), request.getTargetHousing(), request.getIncomeRequirement(),
                request.getApplicantRequirement(), request.getRemarks(), request.getActive());

        LoanProduct updatedProduct = loanProductRepository.save(product);

        log.info("Loan product updated: {}", id);

        // 캐시 무효화
        invalidateProductCache(id);
        invalidateListCache();

        return LoanProductResponse.success(LoanProductDTO.from(updatedProduct), "대출상품 수정 성공");
    }

    @Override
    @Transactional
    public void deleteLoanProduct(Long id) {
        // 대출상품 조회
        LoanProduct product = loanProductRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("대출상품을 찾을 수 없습니다"));

//        // 소프트 삭제
//        product.deactivate();
//        loanProductRepository.save(product);

        // 대출상품 삭제
        loanProductRepository.deleteById(id);

        log.info("Loan product deleted : {}", id);

        // 캐시 무효화
        invalidateProductCache(id);
        invalidateListCache();
    }

    /**
     * 대출상품 데이터 검증
     *
     * @param name 대출이름
     * @param loanLimit 대출한도
     * @param dsrLimit DSR 한도
     * @param interestRate 금리
     */
    private void validateLoanProductData(String name, Long loanLimit, Double dsrLimit, Double interestRate) {
        if (!StringUtils.hasText(name)) {
            throw new ValidationException("대출이름은 필수입니다");
        }
        if (loanLimit == null || loanLimit < 0) {
            throw new ValidationException("대출한도는 0 이상이어야 합니다");
        }
//        if (ltvLimit == null || ltvLimit < 0 || ltvLimit > 100) {
//            throw new ValidationException("LTV 한도는 0-100 사이여야 합니다");
//        }
//        if (dtiLimit == null || dtiLimit < 0 || dtiLimit > 100) {
//            throw new ValidationException("DTI 한도는 0-100 사이여야 합니다");
//        }
        if (dsrLimit == null || dsrLimit < 0 || dsrLimit > 100) {
            throw new ValidationException("DSR 한도는 0-100 사이여야 합니다");
        }
        if (interestRate == null || interestRate < 0 || interestRate > 100) {
            throw new ValidationException("금리는 0-100 사이여야 합니다");
        }
    }

    /**
     * 목록 캐시 키 생성
     *
     * @param housingType 주택유형
     * @param sortBy 정렬 기준
     * @param sortOrder 정렬 순서
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 캐시 키
     */
    private String generateListCacheKey(String housingType, String sortBy, String sortOrder,
                                       String keyword, Pageable pageable) {
        return LOAN_LIST_CACHE_PREFIX +
                (housingType != null ? housingType : "") + ":" +
                (sortBy != null ? sortBy : "") + ":" +
                (sortOrder != null ? sortOrder : "") + ":" +
                (keyword != null ? keyword : "") + ":" +
                pageable.getPageNumber() + ":" +
                pageable.getPageSize();
    }

    /**
     * 목록 캐시 무효화
     */
    private void invalidateListCache() {
        Set<String> keys = redisTemplate.keys(LOAN_LIST_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Invalidated {} loan list cache entries", keys.size());
        }
    }

    /**
     * 상세 캐시 무효화
     *
     * @param id 대출상품 ID
     */
    private void invalidateProductCache(Long id) {
        String cacheKey = LOAN_DETAIL_CACHE_PREFIX + id;
        redisTemplate.delete(cacheKey);
        log.debug("Invalidated loan product cache: {}", id);
    }
}
