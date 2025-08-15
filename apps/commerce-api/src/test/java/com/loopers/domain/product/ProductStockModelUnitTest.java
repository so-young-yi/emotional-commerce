package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;

class ProductStockModelUnitTest {

    @DisplayName("재고 증가/감소 정상 동작")
    @Test
    void stock_increase_and_decrease() {
        ProductStockModel stock = new ProductStockModel(1L, 10L);

        stock.increaseStock(5L);
        assertThat(stock.getStock()).isEqualTo(15L);

        stock.decreaseStock(3L);
        assertThat(stock.getStock()).isEqualTo(12L);
    }

    @DisplayName("재고 증가: 0 이하로 증가 시 예외")
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void increaseStock_shouldFail_whenInvalid(Long qty) {
        ProductStockModel stock = new ProductStockModel(1L, 10L);
        CoreException exception = assertThrows(CoreException.class, () -> stock.increaseStock(qty));
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("재고 차감: 0 이하로 차감 시 예외")
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void decreaseStock_shouldFail_whenInvalid(Long qty) {
        ProductStockModel stock = new ProductStockModel(1L, 10L);
        CoreException exception = assertThrows(CoreException.class, () -> stock.decreaseStock(qty));
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("재고 차감시 부족하면 예외 발생")
    @Test
    void stock_decrease_fail_when_not_enough() {
        ProductStockModel stock = new ProductStockModel(1L, 2L);
        CoreException ex = assertThrows(CoreException.class, () -> stock.decreaseStock(3L));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
