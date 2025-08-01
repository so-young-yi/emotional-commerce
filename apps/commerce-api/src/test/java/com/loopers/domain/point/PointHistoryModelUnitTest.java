package com.loopers.domain.point;

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


class PointHistoryModelUnitTest {

    @DisplayName("userPointId가 null 또는 0 이하이면 예외 발생")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void shouldFail_whenUserPointIdIsInvalid(Long userPointId) {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new PointHistoryModel(userPointId, 1000L, "주문 결제 차감");
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("amount가 null이면 예외 발생")
    @Test
    void shouldFail_whenAmountIsNull() {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new PointHistoryModel(1L, null, "주문 결제 차감");
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("reason이 null 또는 blank면 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void shouldFail_whenReasonIsInvalid(String reason) {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new PointHistoryModel(1L, 1000L, reason);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("정상 생성 케이스")
    @Test
    void shouldSuccess_whenAllValid() {
        PointHistoryModel history = new PointHistoryModel(1L, 1000L, "주문 결제 차감");
        assertThat(history.getUserId()).isEqualTo(1L);
        assertThat(history.getAmount()).isEqualTo(1000L);
        assertThat(history.getReason()).isEqualTo("주문 결제 차감");
        assertThat(history.getCreatedAt()).isNotNull();
    }
}
