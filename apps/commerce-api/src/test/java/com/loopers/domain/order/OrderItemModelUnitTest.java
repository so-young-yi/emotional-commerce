package com.loopers.domain.order;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemModelUnitTest {

    @DisplayName("productId가 null 또는 0 이하이면 예외 발생")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void shouldFail_whenProductIdIsInvalid(Long productId) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new OrderItemModel(productId, 2L, 10000L, "에어포스");
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("quantity가 1 미만이면 예외 발생")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -10})
    void shouldFail_whenQuantityIsInvalid(Long quantity) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new OrderItemModel(1L, quantity, 10000L, "에어포스");
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("priceSnapshot이 null 또는 0 미만이면 예외 발생")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {-1, 0, -10000})
    void shouldFail_whenPriceSnapshotIsInvalid(Long priceSnapshot) {

        CoreException exception = assertThrows(CoreException.class, () -> {
            new OrderItemModel(1L, 2L, priceSnapshot, "에어포스");
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("productNameSnapshot이 null 또는 blank면 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldFail_whenProductNameSnapshotIsInvalid(String name) {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new OrderItemModel(1L, 2L, 10000L, name);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

    }

    @DisplayName("정상 생성 케이스")
    @Test
    void shouldSuccess_whenAllValid() {

        OrderItemModel item = new OrderItemModel(1L, 2L, 10000L, "에어포스");
        assertThat(item.getProductId()).isEqualTo(1L);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getPriceSnapshot()).isEqualTo(10000L);
        assertThat(item.getProductNameSnapshot()).isEqualTo("에어포스");
    }
}
