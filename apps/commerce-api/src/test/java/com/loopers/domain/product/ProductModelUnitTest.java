package com.loopers.domain.product;

import com.loopers.support.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductModelUnitTest {

    @DisplayName("brandId가 null 또는 0 이하이면 예외 발생")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void shouldFail_whenBrandIdIsInvalid(Long brandId) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel( brandId, "에어포스", "에어포스설명", new Money(10000L), ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("상품명이 null 또는 blank면 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldFail_whenNameIsNullOrBlank(String name) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, name, "에어포스설명", new Money(10000L), ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("판매가격이 null이면 예외 발생")
    @Test
    void shouldFail_whenSellPriceIsNull() {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, "에어포스", "에어포스설명", null, ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("상품 가격이 0 이하이면 예외 발생")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10000})
    void shouldFail_whenPriceIsNotPositive(int price) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, "에어포스", "에어포스설명", new Money(price), ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("상태가 null이면 예외 발생")
    @Test
    void shouldFail_whenStatusIsNull() {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, "에어포스", "에어포스설명",  new Money(10000L), null );
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("정상 생성 케이스")
    @Test
    void shouldSuccess_whenAllValid() {
        // arrange & act
        ProductModel product = new ProductModel(1L, "에어포스", "에어포스설명", new Money(10000L), ProductStatus.ON_SALE);

        // assert
        assertThat(product.getBrandId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("에어포스");
        assertThat(product.getSellPrice().getAmount()).isEqualTo(10000L);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

}
