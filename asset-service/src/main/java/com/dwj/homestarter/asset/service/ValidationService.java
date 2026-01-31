package com.dwj.homestarter.asset.service;

import com.dwj.homestarter.asset.dto.request.CreateAssetRequest;
import com.dwj.homestarter.asset.dto.request.UpdateAssetRequest;
import com.dwj.homestarter.common.exception.ValidationException;
import org.springframework.stereotype.Service;

/**
 * 자산정보 검증 서비스
 *
 * 자산정보의 유효성을 검증하는 로직을 담당
 *
 * @author homestarter
 * @since 1.0.0
 */
@Service
public class ValidationService {

    /**
     * 자산정보 생성 요청 검증
     * 배우자 없음일 경우 빈 배열 허용
     *
     * @param request 생성 요청 DTO
     * @throws ValidationException 검증 실패 시
     */
    public void validateAssetRequest(CreateAssetRequest request) {
        if (request == null) {
            throw new ValidationException("자산정보 요청이 null입니다");
        }

        // null 체크만 수행 (빈 배열은 허용 - 배우자 없음 케이스)
        if (request.getAssets() == null) {
            throw new ValidationException("자산 항목이 null입니다");
        }

        if (request.getLoans() == null) {
            throw new ValidationException("대출 항목이 null입니다");
        }

        if (request.getMonthlyIncomes() == null) {
            throw new ValidationException("월소득 항목이 null입니다");
        }

        if (request.getMonthlyExpenses() == null) {
            throw new ValidationException("월지출 항목이 null입니다");
        }

        // 금액 검증 (0 이상)
        request.getAssets().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("자산 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });

        request.getLoans().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("대출 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });

        request.getMonthlyIncomes().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("월소득 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });

        request.getMonthlyExpenses().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("월지출 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });
    }

    /**
     * 자산정보 수정 요청 검증
     * 배우자 없음일 경우 빈 배열 허용
     *
     * @param request 수정 요청 DTO
     * @throws ValidationException 검증 실패 시
     */
    public void validateUpdateRequest(UpdateAssetRequest request) {
        if (request == null) {
            throw new ValidationException("자산정보 요청이 null입니다");
        }

        // null 체크만 수행 (빈 배열은 허용 - 배우자 없음 케이스)
        if (request.getAssets() == null) {
            throw new ValidationException("자산 항목이 null입니다");
        }

        if (request.getLoans() == null) {
            throw new ValidationException("대출 항목이 null입니다");
        }

        if (request.getMonthlyIncomes() == null) {
            throw new ValidationException("월소득 항목이 null입니다");
        }

        if (request.getMonthlyExpenses() == null) {
            throw new ValidationException("월지출 항목이 null입니다");
        }

        // 금액 검증 (0 이상)
        request.getAssets().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("자산 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });

        request.getLoans().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("대출 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });

        request.getMonthlyIncomes().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("월소득 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });

        request.getMonthlyExpenses().forEach(item -> {
            if (item.getAmount() < 0) {
                throw new ValidationException("월지출 금액은 0 이상이어야 합니다: " + item.getName());
            }
        });
    }
}
