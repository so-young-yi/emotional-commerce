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
            new ProductModel(1L, brandId, "에어포스", "에어포스설명", new Money(10000L), 10L, ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("상품명이 null 또는 blank면 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldFail_whenNameIsNullOrBlank(String name) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, 1L, name, "에어포스설명", new Money(10000L), 10L, ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("판매가격이 null이면 예외 발생")
    @Test
    void shouldFail_whenSellPriceIsNull() {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, 1L, "에어포스", "에어포스설명", null, 10L, ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("상품 가격이 0 이하이면 예외 발생")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10000})
    void shouldFail_whenPriceIsNotPositive(int price) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, 1L, "에어포스", "에어포스설명", new Money(price), 10L, ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("재고가 음수면 예외 발생")
    @ParameterizedTest
    @ValueSource(longs = {-1, -100})
    void shouldFail_whenStockIsNegative(Long stock) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, 1L, "에어포스", "에어포스설명",  new Money(10000L), stock, ProductStatus.ON_SALE);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("상태가 null이면 예외 발생")
    @Test
    void shouldFail_whenStatusIsNull() {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, 1L, "에어포스", "에어포스설명",  new Money(10000L), 100L, null );
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("판매중인데 재고가 0이면 예외 발생")
    @Test
    void shouldFail_whenOnSaleButStockIsZero() {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new ProductModel(1L, 1L, "에어포스", "에어포스설명",  new Money(10000L), 0L,  ProductStatus.ON_SALE );
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("정상 생성 케이스")
    @Test
    void shouldSuccess_whenAllValid() {
        // arrange & act
        ProductModel product = new ProductModel(1L, 1L, "에어포스", "에어포스설명", new Money(10000L), 10L, ProductStatus.ON_SALE);

        // assert
        assertThat(product.getBrandId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("에어포스");
        assertThat(product.getSellPrice().getAmount()).isEqualTo(10000L);
        assertThat(product.getStock()).isEqualTo(10);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @DisplayName("상품 재고 차감: 정상 차감")
    @Test
    void decreaseQuantity_shouldSucceed_whenEnoughStock() {

        // arrange
        ProductModel product = new ProductModel(1L, 1L, "에어포스", "에어포스설명", new Money(10000L), 10L, ProductStatus.ON_SALE);

        // act
        product.decreaseStock(3L);

        // assert
        assertThat(product.getStock()).isEqualTo(7);
    }

    @DisplayName("상품 재고 차감: 재고 부족시 예외")
    @Test
    void decreaseQuantity_shouldFail_whenNotEnoughStock() {

        // arrange
        ProductModel product = new ProductModel(1L, 1L, "에어포스", "에어포스설명", new Money(10000L), 2L, ProductStatus.ON_SALE);

        // act & assert
        CoreException exception = assertThrows(CoreException.class, () -> {
            product.decreaseStock(3L);
        });
        assertThat(exception.getMessage()).contains("재고 부족");
    }

    @DisplayName("상품 재고 차감: 주문 불가 상태면 예외")
    @Test
    void decreaseQuantity_shouldFail_whenNotOrderable() {

        // arrange
        ProductModel product = new ProductModel(1L, 1L, "에어포스", "에어포스설명", new Money(10000L), 10L, ProductStatus.STOPPED);

        // act & assert
        CoreException exception = assertThrows(CoreException.class, () -> {
            product.decreaseStock(1L);
        });
        assertThat(exception.getMessage()).contains("주문 불가");
    }

    @DisplayName("상품 상태가 판매중이 아니면 주문 불가")
    @ParameterizedTest
    @ValueSource(strings = {"SOLD_OUT", "STOPPED"})
    void isAvailable_shouldReturnFalse_whenNotOnSale(String status) {

        // arrange
        ProductModel product = new ProductModel(1L, 1L, "에어포스", "에어포스설명", new Money(10000L), 10L, ProductStatus.valueOf(status));

        // act & assert
        assertThat(product.isOrderable()).isFalse();
    }

    @DisplayName("상품 상태가 판매중이고 재고가 1개 이상이면 주문 가능")
    @Test
    void isAvailable_shouldReturnTrue_whenOnSaleAndStock() {

        // arrange
        ProductModel product = new ProductModel(1L, 1L, "에어포스", "에어포스설명", new Money(10000L), 5L, ProductStatus.ON_SALE);

        // act & assert
        assertThat(product.isOrderable()).isTrue();
    }
}
