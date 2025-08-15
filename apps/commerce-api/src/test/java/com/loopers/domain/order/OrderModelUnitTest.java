package com.loopers.domain.order;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderModelUnitTest {

    @DisplayName("userId가 null 또는 0 이하이면 예외 발생")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void shouldFail_whenUserIdIsInvalid(Long userId) {
        // act & assert
        CoreException exception = assertThrows(CoreException.class, () -> {
            new OrderModel(userId, OrderStatus.ORDERED);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("status가 null이면 예외 발생")
    @Test
    void shouldFail_whenStatusIsNull() {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new OrderModel(1L, null);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

    }

    @DisplayName("정상 생성 케이스")
    @Test
    void shouldSuccess_whenAllValid() {
        // arrange & act
        OrderModel order = new OrderModel(1L, OrderStatus.ORDERED);

        // assert
        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(order.getOrderItems()).isEmpty();

    }

    @DisplayName("주문상품 추가: 정상 추가")
    @Test
    void addOrderItem_shouldSucceed_whenValid() {
        // arrange
        OrderModel order = new OrderModel(1L, OrderStatus.ORDERED);
        OrderItemModel item = new OrderItemModel(1L, 2L, 10000L, "에어포스");

        // act
        order.addOrderItem(item);

        // assert
        assertThat(order.getOrderItems()).contains(item);
        assertThat(item.getOrder()).isEqualTo(order);

    }

}
