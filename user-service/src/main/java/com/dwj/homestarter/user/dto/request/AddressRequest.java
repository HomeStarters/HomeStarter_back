package com.dwj.homestarter.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 요청 DTO
 *
 * @author homestarter
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    /**
     * 도로명 주소
     */
    @NotBlank(message = "도로명 주소는 필수입니다")
    private String roadAddress;

    /**
     * 지번 주소
     */
    @NotBlank(message = "지번 주소는 필수입니다")
    private String jibunAddress;

    /**
     * 우편번호
     */
    @NotBlank(message = "우편번호는 필수입니다")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다")
    private String postalCode;

    /**
     * 위도
     */
    @NotNull(message = "위도는 필수입니다")
    private Double latitude;

    /**
     * 경도
     */
    @NotNull(message = "경도는 필수입니다")
    private Double longitude;
}
