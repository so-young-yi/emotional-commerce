package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandModelUnitTest {

    @DisplayName("브랜드명이 null 또는 blank면 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldFail_whenNameIsNullOrBlank(String name) {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new BrandModel(name, "설명");
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("브랜드명이 100자를 초과하면 예외 발생")
    @Test
    void shouldFail_whenNameIsTooLong() {
        String longName = "A".repeat(101);
        CoreException exception = assertThrows(CoreException.class, () -> {
            new BrandModel(longName, "설명");
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("브랜드 설명이 1000자를 초과하면 예외 발생")
    @Test
    void shouldFail_whenDescriptionIsTooLong() {
        String longDesc = "A".repeat(1001);
        CoreException exception = assertThrows(CoreException.class, () -> {
            new BrandModel("나이키", longDesc);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("정상 생성 케이스")
    @Test
    void shouldSuccess_whenAllValid() {
        BrandModel brand = new BrandModel("나이키", "스포츠 브랜드");
        assertThat(brand.getName()).isEqualTo("나이키");
        assertThat(brand.getDescription()).isEqualTo("스포츠 브랜드");
    }
}
