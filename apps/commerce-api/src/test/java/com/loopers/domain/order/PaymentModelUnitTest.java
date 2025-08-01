package com.loopers.domain.order;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentModelUnitTest {

    @DisplayName("orderId가 null 또는 0 이하이면 예외 발생")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -1})
    void shouldFail_whenOrderIdIsInvalid(Long orderId) {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new PaymentModel(orderId, 10000L, ZonedDateTime.now(), PaymentStatus.PAID);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("amount가 null 또는 1 미만이면 예외 발생")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0, -100})
    void shouldFail_whenAmountIsInvalid(Long amount) {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new PaymentModel(1L, amount, ZonedDateTime.now(), PaymentStatus.PAID);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("paidAt이 null이면 예외 발생")
    @Test
    void shouldFail_whenPaidAtIsNull() {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new PaymentModel(1L, 10000L, null, PaymentStatus.PAID);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("status가 null이면 예외 발생")
    @Test
    void shouldFail_whenStatusIsNull() {
        CoreException exception = assertThrows(CoreException.class, () -> {
            new PaymentModel(1L, 10000L, ZonedDateTime.now(), null);
        });
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("정상 생성 케이스")
    @Test
    void shouldSuccess_whenAllValid() {
        PaymentModel payment = new PaymentModel(1L, 10000L, ZonedDateTime.now(), PaymentStatus.PAID);
        assertThat(payment.getOrderId()).isEqualTo(1L);
        assertThat(payment.getAmount()).isEqualTo(10000L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @DisplayName("pay() 호출 시 상태가 PAID로 변경되고 paidAt이 갱신된다")
    @Test
    void pay_shouldSetStatusToPaidAndUpdatePaidAt() {
        PaymentModel payment = new PaymentModel(1L, 10000L, ZonedDateTime.now().minusDays(1), PaymentStatus.FAILED);
        payment.pay();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @DisplayName("refund() 호출 시 상태가 REFUNDED로 변경된다")
    @Test
    void refund_shouldSetStatusToRefunded() {
        PaymentModel payment = new PaymentModel(1L, 10000L, ZonedDateTime.now(), PaymentStatus.PAID);
        payment.refund();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }
}
